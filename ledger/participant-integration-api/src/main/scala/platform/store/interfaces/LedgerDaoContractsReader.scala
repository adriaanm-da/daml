// Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.platform.store.interfaces

import java.time.Instant

import com.daml.lf.data.Ref.Party
import com.daml.lf.transaction.GlobalKey
import com.daml.logging.LoggingContext
import com.daml.platform.store.cache.MutableCacheBackedContractStore.EventSequentialId
import com.daml.platform.store.interfaces.LedgerDaoContractsReader._

import scala.concurrent.Future

private[platform] trait LedgerDaoContractsReader {

  /** Returns the largest ledger time of any of the given contracts.
    *
    * @param ids the contract ids for which to resolve the maximum ledger time
    * @return the optional [[Instant]] maximum ledger time
    */
  def lookupMaximumLedgerTime(ids: Set[ContractId])(implicit
      loggingContext: LoggingContext
  ): Future[Option[Instant]]

  /** Looks up an active or divulged contract if it is visible for the given party.
    *
    * TODO append-only: Remove optionality of `ledgerEndSequentialId` when it's no longer needed for compatibility with the legacy `dao`
    *
    * @param readers a set of parties for one of which the contract must be visible
    * @param contractId the contract id to query
    * @param ledgerEndSequentialId optionally, the ledger end sequential id against which the query should be executed
    * @return the optional [[Contract]] value
    */
  def lookupActiveContractAndLoadArgument(
      readers: Set[Party],
      contractId: ContractId,
      ledgerEndSequentialId: Option[EventSequentialId] = None,
  )(implicit loggingContext: LoggingContext): Future[Option[Contract]]

  /** Looks up an active or divulged contract if it is visible for the given party.
    * This method uses the provided create argument for building the [[Contract]] value
    * instead of decoding it again.
    *
    * TODO append-only: Remove optionality of `ledgerEndSequentialId` when it's no longer needed for compatibility with the legacy `dao`
    *
    * @param readers a set of parties for one of which the contract must be visible
    * @param contractId the contract id to query
    * @param createArgument the contract create argument
    * @param ledgerEndSequentialId optionally, the ledger end sequential id against which the query should be executed
    * @return the optional [[Contract]] value
    */
  def lookupActiveContractWithCachedArgument(
      readers: Set[Party],
      contractId: ContractId,
      createArgument: Value,
      ledgerEndSequentialId: Option[EventSequentialId] = None,
  )(implicit loggingContext: LoggingContext): Future[Option[Contract]]

  /** Looks up a Contract given a contract key and a party
    *
    * @param key the contract key to query
    * @param forParties a set of parties for one of which the contract must be visible
    * @return the optional [[ContractId]]
    */
  def lookupContractKey(
      key: GlobalKey,
      forParties: Set[Party],
  )(implicit loggingContext: LoggingContext): Future[Option[ContractId]]

  /** Looks up the contract by id at a specific ledger event sequential id.
    *
    * @param contractId the contract id to query
    * @param validAt the event sequential id at which to resolve the contract state
    * @return the optional [[ContractState]]
    */
  def lookupContractState(contractId: ContractId, validAt: Long)(implicit
      loggingContext: LoggingContext
  ): Future[Option[ContractState]]

  /** Looks up the state of a contract key at a specific event sequential id.
    *
    * @param key the contract key to query
    * @param validAt the event sequential id at which to resolve the key state
    * @return the [[KeyState]]
    */
  def lookupKeyState(key: GlobalKey, validAt: Long)(implicit
      loggingContext: LoggingContext
  ): Future[KeyState]
}

object LedgerDaoContractsReader {
  import com.daml.lf.value.{Value => lfval}
  private type ContractId = lfval.ContractId
  private type Value = lfval.VersionedValue[ContractId]
  private type Contract = lfval.ContractInst[Value]

  sealed trait ContractState extends Product with Serializable {
    def stakeholders: Set[Party]
  }

  final case class ActiveContract(
      contract: Contract,
      stakeholders: Set[Party],
      ledgerEffectiveTime: Instant,
  ) extends ContractState

  final case class ArchivedContract(stakeholders: Set[Party]) extends ContractState

  sealed trait KeyState extends Product with Serializable

  final case class KeyAssigned(contractId: ContractId, stakeholders: Set[Party]) extends KeyState

  final case object KeyUnassigned extends KeyState
}
