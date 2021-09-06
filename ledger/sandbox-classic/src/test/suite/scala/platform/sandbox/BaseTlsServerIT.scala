// Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.platform.sandbox

import com.daml.bazeltools.BazelRunfiles._
import com.daml.ledger.api.testing.utils.SuiteResourceManagementAroundAll
import com.daml.ledger.api.tls.TlsVersion.TlsVersion
import com.daml.ledger.api.tls.{TlsConfiguration, TlsVersion}
import com.daml.ledger.api.v1.transaction_service.GetLedgerEndResponse
import com.daml.ledger.client.LedgerClient
import com.daml.ledger.client.configuration.{CommandClientConfiguration, LedgerClientConfiguration, LedgerIdRequirement}
import com.daml.platform.sandbox.config.SandboxConfig
import com.daml.platform.sandbox.services.SandboxFixture
import io.grpc.StatusRuntimeException
import org.scalatest.Assertion
import org.scalatest.wordspec.AsyncWordSpec

import java.io.File
import java.net.ConnectException
import scala.concurrent.Future

abstract class BaseTlsServerIT extends AsyncWordSpec with SandboxFixture with SuiteResourceManagementAroundAll {

  protected val List(
    certChainFilePath,
    privateKeyFilePath,
    trustCertCollectionFilePath,
    clientCertChainFilePath,
    clientPrivateKeyFilePath,
  ) = {
    List("server.crt", "server.pem", "ca.crt", "client.crt", "client.pem").map { src =>
      new File(rlocation("ledger/test-common/test-certificates/" + src))
    }
  }

  private lazy val baseClientConfig: LedgerClientConfiguration =
    LedgerClientConfiguration(
      "appId",
      LedgerIdRequirement.none,
      CommandClientConfiguration.default,
      None,
    )

  private def tlsEnabledClientConfig(minimumRequiredProtocolVersion: TlsVersion): LedgerClientConfiguration =
    baseClientConfig.copy(sslContext =
      TlsConfiguration(
        enabled = true,
        Some(clientCertChainFilePath),
        Some(clientPrivateKeyFilePath),
        Some(trustCertCollectionFilePath),
        minimumProtocolVersion = minimumRequiredProtocolVersion,
      ).client
    )

  override protected lazy val config: SandboxConfig =
    super.config.copy(
      tlsConfig = Some(
        TlsConfiguration(
          enabled = true,
          Some(certChainFilePath),
          Some(privateKeyFilePath),
          Some(trustCertCollectionFilePath),
          minimumProtocolVersion = TlsVersion.V1_3
        )
      )
    )

  protected def testWhenClientWithoutSsl(): Future[Assertion] = {
    recoverToSucceededIf[StatusRuntimeException] {
      LedgerClient
        .singleHost(serverHost, serverPort.value, baseClientConfig)
        .flatMap(_.transactionClient.getLedgerEnd())
    }
  }

  protected def testSuccessfulConnection(clientTlsVersion: TlsVersion): Future[Assertion] = {
    testClientWith(clientTlsVersion).map(response => assert(
      (response ne null)
        && response.isInstanceOf[GetLedgerEndResponse]))
  }

  protected def testFailedConnection(clientTlsVersion: TlsVersion): Future[Assertion] = {
    val fe: Future[StatusRuntimeException] = recoverToExceptionIf[StatusRuntimeException] {
      testClientWith(clientTlsVersion)
    }

    fe.map { e =>
      assert(e.getCause.isInstanceOf[ConnectException])
      assert(e.getCause.getMessage.startsWith("Connection refused"))
    }
  }

  protected def clientF(protocol: TlsVersion): Future[LedgerClient] =
    LedgerClient.singleHost(serverHost, serverPort.value, tlsEnabledClientConfig(protocol))

  protected def testClientWith(protocol: TlsVersion): Future[GetLedgerEndResponse] =
    withClue(s"Testing with $protocol") {
      clientF(protocol).flatMap(_.transactionClient.getLedgerEnd())
    }
}

class TlsDisabledServerIT extends BaseTlsServerIT {
  "A server with TLS disabled" should {
    "reject ledger queries when the client connects without tls" in {
      testWhenClientWithoutSsl()
    }

    "connect when client enabled TLS 1.2 or higher" in {
      for {
        _ <- testSuccessfulConnection(TlsVersion.Default)
      } yield succeed
    }

    "fail to connect when client enabled any TLS" in {
      for {
        _ <- testFailedConnection(TlsVersion.V1)
        _ <- testFailedConnection(TlsVersion.V1_1)
        _ <- testFailedConnection(TlsVersion.V1_2)
        _ <- testFailedConnection(TlsVersion.V1_3)
      } yield succeed
    }
  }
}

class Tls1_2EnabledServerIT extends BaseTlsServerIT {
  "A server with only TLS 1.2 enabled" should {
    "reject ledger queries when the client connects without tls" in {
      testWhenClientWithoutSsl()
    }

    "connect when client enabled TLS 1.2 or higher" in {
      for {
        _ <- testSuccessfulConnection(TlsVersion.V1_2)
        _ <- testSuccessfulConnection(TlsVersion.V1_3)
      } yield succeed
    }

    "fail to connect when client enabled TLS 1.1 or lower" in {
      for {
        _ <- testFailedConnection(TlsVersion.V1)
        _ <- testFailedConnection(TlsVersion.V1_1)
      } yield succeed
    }
  }
}

class Tls1_3EnabledServerIT extends BaseTlsServerIT {
  "A server with only TLS 1.3 enabled" should {
    "reject ledger queries when the client connects without tls" in {
      testWhenClientWithoutSsl()
    }

    "connect when client enabled TLS 1.3 or higher" in {
      for {
        _ <- testSuccessfulConnection(TlsVersion.V1_3)
      } yield succeed
    }

    "fail to connect when client enabled TLS 1.2 or lower" in {
      for {
        _ <- testFailedConnection(TlsVersion.V1)
        _ <- testFailedConnection(TlsVersion.V1_1)
        _ <- testFailedConnection(TlsVersion.V1_2)
      } yield succeed
    }
  }
}