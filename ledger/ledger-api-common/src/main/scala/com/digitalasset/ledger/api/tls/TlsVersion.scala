package com.daml.ledger.api.tls


object TlsVersion {

  sealed abstract class TlsVersion(val version: String)

  /**
   * Defaults to whatever underlying implementation (e.g. Netty's) defaults to.
   */
  case object Default extends TlsVersion("<Default>")

  case object V1 extends TlsVersion("TLSv1")

  case object V1_1 extends TlsVersion("TLSv1.1")

  case object V1_2 extends TlsVersion("TLSv1.2")

  case object V1_3 extends TlsVersion("TLSv1.3")

}
