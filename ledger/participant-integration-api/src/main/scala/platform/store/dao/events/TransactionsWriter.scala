// Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.platform.store.dao.events

import java.sql.Connection
import java.time.Instant

import anorm.{Row, SimpleSql}
import com.daml.ledger.participant.state.v1.{
  CommittedTransaction,
  DivulgedContract,
  Offset,
  SubmitterInfo,
}
import com.daml.ledger.{TransactionId, WorkflowId}
import com.daml.lf.engine.Blinding
import com.daml.lf.transaction.BlindingInfo
import com.daml.metrics.{Metrics, Timed}
import com.daml.platform.store.DbType

private[platform] object TransactionsWriter {
  private[platform] class PreparedInsert(
                                          eventsTableExecutables: EventsTable.Batches,
                                          contractsTableExecutables: ContractsTable.Executables,
                                          contractWitnessesTableExecutables: ContractWitnessesTable.Executables,
                                          transactionCompletionTables: TransactionCompletionTables.Executables,
  ) {

    def completeTransaction(metrics: Metrics)(implicit conn: Connection): Unit = {
      import metrics.daml.index.db.storeTransactionDbMetrics._

      Timed.value(eventsBatch, transactionCompletionTables.execute)
    }

    def write(metrics: Metrics)(implicit connection: Connection): Unit = {
      writeEvents(metrics)
      writeState(metrics)
    }

    def writeEvents(metrics: Metrics)(implicit connection: Connection): Unit =
      Timed.value(
        metrics.daml.index.db.storeTransactionDbMetrics.eventsBatch,
        eventsTableExecutables.executeEventsInsert(),
      )

    def writeState(metrics: Metrics)(implicit connection: Connection): Unit = {
      import metrics.daml.index.db.storeTransactionDbMetrics._

      // Delete the witnesses of contracts that being removed first, to
      // respect the foreign key constraint of the underlying storage
      for (deleteWitnesses <- contractWitnessesTableExecutables.deleteWitnesses) {
        Timed.value(deleteContractWitnessesBatch, deleteWitnesses.execute())
      }

      for (deleteContracts <- contractsTableExecutables.deleteContracts) {
        Timed.value(deleteContractsBatch, deleteContracts.execute())
      }

      for(insertContracts <- contractsTableExecutables.insertContracts) {
        Timed.value(insertContractsBatch, insertContracts.execute())
      }

      // Insert the witnesses last to respect the foreign key constraint of the underlying storage.
      // Compute and insert new witnesses regardless of whether the current transaction adds new
      // contracts because it may be the case that we are only adding new witnesses to existing
      // contracts (e.g. via divulging a contract with fetch).
      val _ = Timed.value(
        insertContractWitnessesBatch,
        contractWitnessesTableExecutables.insertWitnesses.execute(),
      )
    }
  }
}

private[platform] final class TransactionsWriter(
    dbType: DbType,
    metrics: Metrics,
    lfValueTranslation: LfValueTranslation,
    compressionStrategy: CompressionStrategy,
    compressionMetrics: CompressionMetrics,
    idempotentEventInsertions: Boolean = false,
) {

  private val eventsTable = EventsTable(dbType, idempotentEventInsertions)
  private val contractsTable = ContractsTable(dbType)
  private val contractWitnessesTable = ContractWitnessesTable(dbType)

  def prepareEntry(
                    submitterInfo: Option[SubmitterInfo],
                    workflowId: Option[WorkflowId],
                    transactionId: TransactionId,
                    ledgerEffectiveTime: Instant,
                    offset: Offset,
                    transaction: CommittedTransaction,
                    divulgedContracts: Iterable[DivulgedContract],
                    blindingInfo: Option[BlindingInfo],
                  ): PreparedRawEntry = {
    val blinding = blindingInfo.getOrElse(Blinding.blind(transaction))

    val indexing =
      TransactionIndexing.from(
        blinding,
        submitterInfo,
        workflowId,
        transactionId,
        ledgerEffectiveTime,
        offset,
        transaction,
        divulgedContracts,
      )

    val serialized =
      Timed.value(
        metrics.daml.index.db.storeTransactionDbMetrics.translationTimer,
        TransactionIndexing.serialize(
          lfValueTranslation,
          transactionId,
          indexing.events.events,
          indexing.contracts.divulgedContracts,
        ),
      )

    val compressed =
      Timed.value(
        metrics.daml.index.db.storeTransactionDbMetrics.compressionTimer,
        TransactionIndexing.compress(
          serialized,
          compressionStrategy,
          compressionMetrics,
        ),
      )

    PreparedRawEntry(
      tx = indexing.transaction,
      events = indexing.events,
      compressed = compressed,
      contracts = indexing.contracts,
      contractWitnesses = indexing.contractWitnesses
    )
  }

  def prepareInsert(preparedRawEntries: Seq[PreparedRawEntry]): TransactionsWriter.PreparedInsert =
    new TransactionsWriter.PreparedInsert(
      eventsTable.toExecutables(preparedRawEntries),
      contractsTable.toExecutables(preparedRawEntries),
      contractWitnessesTable.toExecutables(preparedRawEntries),
      TransactionCompletionTables.toExecutables(preparedRawEntries)
    )

  def prepareEventsDelete(endInclusive: Offset): SimpleSql[Row] =
    EventsTableDelete.prepareEventsDelete(endInclusive)

}
