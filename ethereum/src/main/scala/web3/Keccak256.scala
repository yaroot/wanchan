package web3

import _root_.web3.hash.Keccak as KeccakJ
import scodec.bits.ByteVector

import java.nio.charset.StandardCharsets

object Keccak256 {
  def digest(bytes: ByteVector): ByteVector = {
    val x = new KeccakJ(256)
    x.update(bytes.toArray)
    ByteVector(x.digestArray())
  }

  def digestString(str: String): ByteVector = {
    val x = new KeccakJ(256)
    x.update(str.getBytes(StandardCharsets.UTF_8))
    ByteVector(x.digestArray())
  }
}
