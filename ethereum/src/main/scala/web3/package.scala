package web3

import cats.implicits.*
import scodec.bits.ByteVector
import io.circe.*
import web3.SizedByteString.Type

import scala.annotation.{tailrec, targetName}

opaque type Bytes   = ByteVector
opaque type Address = SizedByteString.Type[20L]
opaque type TxHash  = SizedByteString.Type[32L]
opaque type UInt256 = SizedUInt.Type[256L]
opaque type UInt64  = SizedUInt.Type[64L]

def strip0x(x: String): String =
  if (x.startsWith("0x")) x.substring(2) else x

object Address:
  val Zero: Address = unsafeFromString("0", true)
  val Dead: Address = unsafeFromString("DEAD", true)

  def unsafeFromString(x: String, pad: Boolean = false): Address =
    SizedByteString.unsafeFromString[20L](x, pad)

  def fromString(x: String, pad: Boolean = false): Option[Address] =
    SizedByteString.fromString[20L](x, pad)

  def fromBytes(x: ByteVector, pad: Boolean = false): Option[Address] =
    SizedByteString.fromBytes[20L](x, pad)

  implicit val circeEncoder: Encoder[Address] = SizedByteString.circeEncoderFor[20L]
  implicit val circeDecoder: Decoder[Address] = SizedByteString.circeDecoderFor[20L]

  extension (x: Address)
    def toHex: String          = SizedByteString.toHex(x)
    def toHexRaw: String       = SizedByteString.toHexRaw(x)
    def checksumFormat: String = {
      // EIP-55 checksum encoding

      val builder = new scala.collection.mutable.StringBuilder(42)
      builder += '0'
      builder += 'x'

      val hexAddr    = x.toHex
      val hashedAddr = Keccak256.digestString(hexAddr).toHex

      @tailrec
      def go(n: Int): String = n match {
        case 40 => builder.result()
        case _  =>
          val c      = hexAddr.charAt(n)
          val nibble = hashedAddr.charAt(n)
          if (nibble > '7') builder += c.toUpper
          else builder += c
          go(n + 1)
      }

      go(0)
    }

object TxHash:
  implicit val circeEncoder: Encoder[TxHash] = SizedByteString.circeEncoderFor[32L]
  implicit val circeDecoder: Decoder[TxHash] = SizedByteString.circeDecoderFor[32L]
  extension (x: TxHash)
    def toHex: String    = SizedByteString.toHex(x)
    def toHexRaw: String = SizedByteString.toHexRaw(x)

object Bytes:
  implicit val circeEncoder: Encoder[Bytes] = Encoder[String].contramap(_.toHex)
  implicit val circeDecoder: Decoder[Bytes] = Decoder[String].emap { x =>
    ByteVector.fromHex(x).fold("Cannot decode hex encoded bytes".asLeft[Bytes])(_.asRight)
  }
  extension (x: Bytes)
    def toHex: String    = "0x" + x.toHex
    def toHexRaw: String = x.toHex

object UInt256:
  extension (x: UInt256)
    def toHex: String    = SizedUInt.toHex[256L](x)
    def toHexRaw: String = SizedUInt.toHexRaw[256L](x)

object UInt64:
  extension (x: UInt64)
    def toHex: String    = SizedUInt.toHex[64L](x)
    def toHexRaw: String = SizedUInt.toHexRaw[64L](x)
