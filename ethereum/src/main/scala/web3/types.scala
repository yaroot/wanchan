package web3

import cats.implicits.*
import scodec.bits.ByteVector
import io.circe.*
import scala.util.Try

trait Sized[S]

object SizedByteString {
  type Type[S] = ByteVector & Sized[S]

  def tag[S](x: ByteVector): Type[S] = x.asInstanceOf[Type[S]]

  def padLeft(bv: ByteVector, size: Long): ByteVector = {
    if (size > bv.size) {
      ByteVector.fill(size - bv.size)(0) ++ bv
    } else {
      bv
    }
  }

  def fromBytes[S <: Long](input: ByteVector, pad: Boolean = false)(implicit v: ValueOf[S]): Option[Type[S]] = {
    val a = if (pad) padLeft(input, v.value) else input
    (a.size == v.value).guard[Option].as(tag[S](a))
  }

  def fromString[S <: Long](input: String, pad: Boolean = false)(implicit v: ValueOf[S]): Option[Type[S]] = {
    ByteVector.fromHex(input).flatMap(fromBytes[S](_, pad))
  }

  def unsafeFromString[S <: Long](input: String, pad: Boolean = false)(implicit v: ValueOf[S]): Type[S] = {
    fromString[S](input, pad).getOrElse(throw new RuntimeException("Cannot convert input to result type"))
  }

  def toHex[S](x: Type[S]): String = "0x" + x.toHex

  def toHexRaw[S](x: Type[S]): String = x.toHex

  def circeEncoderFor[S <: Long]: Encoder[Type[S]]                         =
    Encoder[String].contramap(toHex[S])
  def circeDecoderFor[S <: Long](implicit v: ValueOf[S]): Decoder[Type[S]] =
    Decoder[String].emap { a =>
      fromString[S](a, pad = true)
        .fold("not HexString".asLeft[Type[S]])(_.asRight)
    }
}

object SizedUInt {
  type Type[S] = BigInt & Sized[S]

  def tag[S](x: BigInt): Type[S] = x.asInstanceOf[Type[S]]

  def validate[S <: Long](input: BigInt)(implicit v: ValueOf[S]): Option[Type[S]] = {
    input.some
      .filter(_.signum >= 0)         // positive
      .filter(_.bitCount <= v.value) // size
      .map(tag[S](_))
  }

  def fromLong[S <: Long](input: Long)(implicit v: ValueOf[S]): Option[Type[S]] = {
    validate[S](input)
  }

  def unsafeFromLong[S <: Long](input: Long)(implicit v: ValueOf[S]): Type[S] = {
    fromLong[S](input).getOrElse(throw new java.lang.RuntimeException("Cannot convert input to result type"))
  }

  def fromString[S <: Long](input: String)(implicit v: ValueOf[S]): Option[Type[S]] = {
    val input0 = strip0x(input)
    // starts with 0x or (string length / 2 == bitCount / 8)
    (
      if (input.startsWith("0x") || input0.size * 4 == v.value) {
        Try(BigInt(input0, 16))
      } else {
        Try(BigInt(input0, 10))
      }
    ).toOption.flatMap(validate[S])
  }

  def toHexRaw[S <: Long](input: Type[S])(implicit v: ValueOf[S]): String = {
    val str          = input.toString(16)
    val bitCount     = v.value
    val targetLength = bitCount / 8 * 2
    if (targetLength > str.length)
      Array.fill(targetLength.toInt - str.length)("0").mkString + str
    else
      str
  }

  def toHex[S <: Long](input: Type[S])(implicit v: ValueOf[S]): String =
    "0x" + toHexRaw[S](input)

  def circeEncoderFor[S <: Long](implicit v: ValueOf[S]): Encoder[Type[S]] =
    Encoder[String].contramap(toHex[S](_))

  def circeDecoderFor[S <: Long](implicit v: ValueOf[S]): Decoder[Type[S]] =
    Decoder.instance { c =>
      import io.circe.DecodingFailure.Reason.WrongTypeExpectation
      if (c.value.isNumber) {
        c.value.asNumber
          .flatMap(_.toLong)
          .flatMap(fromLong[S])
          .fold(DecodingFailure(WrongTypeExpectation("UInt", c.value), c.history).asLeft[Type[S]])(_.asRight)
      } else if (c.value.isString) {
        c.value.asString
          .flatMap(fromString[S])
          .fold(DecodingFailure(WrongTypeExpectation("UInt", c.value), c.history).asLeft[Type[S]])(_.asRight)

      } else {
        DecodingFailure(WrongTypeExpectation("UInt", c.value), c.history).asLeft[Type[S]]
      }
    }
}
