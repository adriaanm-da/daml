// Copyright (c) 2020 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.ledger.rxjava.grpc.helpers

import com.digitalasset.ledger.api.auth.Authorizer
import com.digitalasset.ledger.api.auth.services.LedgerConfigurationServiceAuthorization
import com.digitalasset.ledger.api.v1.ledger_configuration_service.LedgerConfigurationServiceGrpc.LedgerConfigurationService
import com.digitalasset.ledger.api.v1.ledger_configuration_service.{
  GetLedgerConfigurationRequest,
  GetLedgerConfigurationResponse,
  LedgerConfigurationServiceGrpc
}
import io.grpc.ServerServiceDefinition
import io.grpc.stub.StreamObserver

import scala.concurrent.ExecutionContext

class LedgerConfigurationServiceImpl(responses: Seq[GetLedgerConfigurationResponse])
    extends LedgerConfigurationService
    with FakeAutoCloseable {

  private var lastRequest: Option[GetLedgerConfigurationRequest] = None

  override def getLedgerConfiguration(
      request: GetLedgerConfigurationRequest,
      responseObserver: StreamObserver[GetLedgerConfigurationResponse]): Unit = {
    this.lastRequest = Some(request)
    responses.foreach(responseObserver.onNext)
  }

  def getLastRequest: Option[GetLedgerConfigurationRequest] = this.lastRequest
}

object LedgerConfigurationServiceImpl {
  def createWithRef(responses: Seq[GetLedgerConfigurationResponse], authorizer: Authorizer)(
      implicit ec: ExecutionContext): (ServerServiceDefinition, LedgerConfigurationServiceImpl) = {
    val impl = new LedgerConfigurationServiceImpl(responses)
    val authImpl = new LedgerConfigurationServiceAuthorization(impl, authorizer)
    (LedgerConfigurationServiceGrpc.bindService(authImpl, ec), impl)
  }
}
