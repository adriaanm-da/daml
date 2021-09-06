// Copyright (c) 2021 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.ledger.api.tls

import com.daml.ledger.api.tls.TlsVersion.{TlsVersion, V1, V1_1, V1_2, V1_3}
import io.grpc.netty.GrpcSslContexts
import io.netty.buffer.ByteBufAllocator
import io.netty.handler.ssl.{ClientAuth, SslContext}
import org.slf4j.LoggerFactory

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}
import java.nio.file.Files
import javax.net.ssl.SSLEngine
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal


final case class TlsInfo(enabledCipherSuites: Seq[String],
                         enabledProtocols: Seq[String],
                         supportedCipherSuites: Seq[String],
                         supportedProtocols: Seq[String])

object TlsInfo {
  def fromSslContext(sslContext: SslContext): TlsInfo = {
    val engine: SSLEngine = sslContext.newEngine(ByteBufAllocator.DEFAULT)
    TlsInfo(
      enabledCipherSuites = engine.getEnabledCipherSuites.toIndexedSeq,
      enabledProtocols = engine.getEnabledProtocols.toIndexedSeq,
      supportedCipherSuites = engine.getSupportedCipherSuites.toIndexedSeq,
      supportedProtocols = engine.getSupportedProtocols.toIndexedSeq,
    )
  }
}

final case class TlsConfiguration(enabled: Boolean,
                                  keyCertChainFile: Option[File] = None, // mutual auth is disabled if null
                                  keyFile: Option[File] = None,
                                  trustCertCollectionFile: Option[File] = None, // System default if null
                                  secretsUrl: Option[SecretsUrl] = None,
                                  clientAuth: ClientAuth =
                                  ClientAuth.REQUIRE, // Client auth setting used by the server. This is not used in the client configuration.
                                  enableCertRevocationChecking: Boolean = false,
                                  minimumProtocolVersion: TlsVersion = TlsVersion.Default,
                                 ) {

  private val logger = LoggerFactory.getLogger(getClass)

  /** If enabled and all required fields are present, it returns an SslContext suitable for client usage */
  def client: Option[SslContext] = {
    if (enabled) {
      val defaultSslContext = GrpcSslContexts
        .forClient()
        .keyManager(
          keyCertChainFile.orNull,
          keyFile.orNull,
        )
        .trustManager(trustCertCollectionFile.orNull)
        .protocols(protocolsNames(null))
        .sslProvider(SslContext.defaultClientProvider())
        .build()
      val tlsInfo0 = TlsInfo.fromSslContext(defaultSslContext)

      val sslContext = GrpcSslContexts
        .forClient()
        .keyManager(
          keyCertChainFile.orNull,
          keyFile.orNull,
        )
        .trustManager(trustCertCollectionFile.orNull)
        .protocols(protocolsNames(tlsInfo0))
        .sslProvider(SslContext.defaultClientProvider())
        .build()
      logTlsProtocolsAndCipherSuites(sslContext)
      Some(sslContext)
    }
    else None
  }

  /** If enabled and all required fields are present, it returns an SslContext suitable for server usage */
  def server: Option[SslContext] =
    if (enabled) {
      scala.util.Using.resources(
        keyCertChainInputStreamOrFail,
        keyInputStreamOrFail,
      ) { (keyCertChain: InputStream, key: InputStream) =>
        val defaultSslContext = GrpcSslContexts
          .forServer(
            keyCertChain,
            key,
          )
          .trustManager(trustCertCollectionFile.orNull)
          .clientAuth(clientAuth)
          .protocols(null)
          .sslProvider(SslContext.defaultServerProvider())
          .build()
        val tlsInfo0 = TlsInfo.fromSslContext(defaultSslContext)

        val sslContext = GrpcSslContexts
          .forServer(
            keyCertChain,
            key,
          )
          .trustManager(trustCertCollectionFile.orNull)
          .clientAuth(clientAuth)
          .protocols(protocolsNames(tlsInfo0))
          .sslProvider(SslContext.defaultServerProvider())
          .build()
        logTlsProtocolsAndCipherSuites(sslContext)
        Some(sslContext)
      }
      //      val x: SslContext = s.get
      //      val _ = x.attributes()
      //      x match {
      //        case jdkContext: io.netty.handler.ssl.JdkSslServerContext => {
      //          ()
      //        }
      //        case openSslContext: io.netty.handler.ssl.OpenSslServerContext => {
      //          openSslContext
      //        }
      //      }
      //      OpenSsl.getS
      //      s.getSupportedSSLParameters().getProtocols()
      //      s
    } else {
      logger.info(s"Server's TLS: Disabled.")
      None
    }


  private[tls] def logTlsProtocolsAndCipherSuites(sslContext: SslContext): Unit = {
    val tlsInfo = TlsInfo.fromSslContext(sslContext)
    logger.info(s"TLS: Enabled.")
    logger.info(s"TLS: Supported protocols: ${tlsInfo.supportedProtocols.mkString(", ")}.")
    logger.info(s"TLS: Enabled protocols: ${tlsInfo.enabledProtocols.mkString(", ")}.")
    logger.info(s"TLS: Supported cipher suites: ${tlsInfo.supportedCipherSuites.mkString(", ")}.")
    logger.info(s"TLS: Enabled cipher suites: ${tlsInfo.enabledCipherSuites.mkString(", ")}.")
  }

  /** This is a side-effecting method. It modifies JVM TLS properties according to the TLS configuration. */
  def setJvmTlsProperties(): Unit =
    if (enabled && enableCertRevocationChecking) OcspProperties.enableOcsp()

  private[tls] def protocolsNames(tlsInfo: TlsInfo): java.lang.Iterable[String] = {
    minimumProtocolVersion match {
      case TlsVersion.Default => null
      case _ => {
        val versions = minimumProtocolVersion match {
          case V1 => Seq[TlsVersion](V1, V1_1, V1_2, V1_3)
          case V1_1 => Seq[TlsVersion](V1_1, V1_2, V1_3)
          case V1_2 => Seq[TlsVersion](V1_2, V1_3)
          case V1_3 => Seq(V1_3)
          case other => throw new IllegalStateException(s"Could not recognize |${other}|!")
        }
        versions
          .map(_.version)
          .filter(tlsInfo.supportedProtocols.contains(_))
          .asJava
      }
    }
  }

  private[tls] def keyInputStreamOrFail: InputStream = {
    val keyFileOrFail = keyFile.getOrElse(
      throw new IllegalArgumentException(
        s"Unable to convert ${this.toString} to SSL Context: cannot create SSL context without keyFile."
      )
    )
    prepareKeyInputStream(keyFileOrFail)
  }

  private[tls] def prepareKeyInputStream(keyFile: File): InputStream = {
    val bytes = if (keyFile.getName.endsWith(".enc")) {
      try {
        val params = DecryptionParameters.fromSecretsServer(secretsUrlOrFail)
        params.decrypt(encrypted = keyFile)
      } catch {
        case NonFatal(e) => throw new PrivateKeyDecryptionException(e)
      }
    } else {
      Files.readAllBytes(keyFile.toPath)
    }
    new ByteArrayInputStream(bytes)
  }

  private def secretsUrlOrFail: SecretsUrl = secretsUrl.getOrElse(
    throw new IllegalStateException(
      s"Unable to convert ${this.toString} to SSL Context: cannot decrypt keyFile without secretsUrl."
    )
  )

  private def keyCertChainInputStreamOrFail: InputStream = {
    val msg =
      s"Unable to convert ${this.toString} to SSL Context: cannot create SSL context without keyCertChainFile."
    val keyFile = keyCertChainFile.getOrElse(throw new IllegalStateException(msg))
    new FileInputStream(keyFile)
  }

}

object TlsConfiguration {
  val Empty: TlsConfiguration = TlsConfiguration(
    enabled = true,
    keyCertChainFile = None,
    keyFile = None,
    trustCertCollectionFile = None,
  )
}
