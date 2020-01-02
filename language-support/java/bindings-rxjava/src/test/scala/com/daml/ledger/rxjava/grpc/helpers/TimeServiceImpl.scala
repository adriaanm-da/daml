// Copyright (c) 2020 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.ledger.rxjava.grpc.helpers

import com.digitalasset.ledger.api.auth.Authorizer
import com.digitalasset.ledger.api.auth.services.TimeServiceAuthorization
import com.digitalasset.ledger.api.v1.testing.time_service.TimeServiceGrpc.TimeService
import com.digitalasset.ledger.api.v1.testing.time_service.{
  GetTimeRequest,
  GetTimeResponse,
  SetTimeRequest,
  TimeServiceGrpc
}
import com.google.protobuf.empty.Empty
import io.grpc.ServerServiceDefinition
import io.grpc.stub.StreamObserver

import scala.concurrent.{ExecutionContext, Future};

final class TimeServiceImpl(getTimeResponses: Seq[GetTimeResponse])
    extends TimeService
    with FakeAutoCloseable {

  private var lastGetTimeRequest: Option[GetTimeRequest] = None
  private var lastSetTimeRequest: Option[SetTimeRequest] = None

  override def getTime(
      request: GetTimeRequest,
      responseObserver: StreamObserver[GetTimeResponse]): Unit = {
    this.lastGetTimeRequest = Some(request)
    getTimeResponses.foreach(responseObserver.onNext)
    responseObserver.onCompleted()
  }

  override def setTime(request: SetTimeRequest): Future[Empty] = {
    this.lastSetTimeRequest = Some(request)
    Future.successful(Empty.defaultInstance)
  }

  def getLastGetTimeRequest: Option[GetTimeRequest] = this.lastGetTimeRequest

  def getLastSetTimeRequest: Option[SetTimeRequest] = this.lastSetTimeRequest
}

object TimeServiceImpl {
  def createWithRef(getTimeResponses: Seq[GetTimeResponse], authorizer: Authorizer)(
      implicit ec: ExecutionContext): (ServerServiceDefinition, TimeServiceImpl) = {
    val impl = new TimeServiceImpl(getTimeResponses)
    val authImpl = new TimeServiceAuthorization(impl, authorizer)
    (TimeServiceGrpc.bindService(authImpl, ec), impl)
  }
}
