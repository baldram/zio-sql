package zio.sql.postgresql

import java.time.LocalDate
import java.util.UUID

import zio.Cause
import zio.test._
import zio.test.Assertion._

object FunctionDefSpec extends PostgresRunnableSpec with ShopSchema {

  import this.Customers._
  import this.PostgresFunctionDef._
  import this.FunctionDef._

  val spec = suite("Postgres FunctionDef")(
    testM("sin") {
      val query = select(Sin(1.0)) from customers

      val expected = 0.8414709848078965

      val testResult = execute(query).to[Double, Double](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("sind") {
      val query = select(Sind(30.0)) from customers

      val expected = 0.5

      val testResult = execute(query).to[Double, Double](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("hex") {
      val query = select(Hex(2147483647)) from customers

      val expected = "7fffffff"
      
      val testResult = execute(query).to[String, String](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("trunc") {
      val query = select(Trunc(42.8)) from customers

      val expected = 42d

      val testResult = execute(query).to[Double, Double](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("length") {
      val query = select(Length("'hello'")) from customers

      val expected = 5

      val testResult = execute(query).to[Int, Int](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("translate") {
      val query = select(Translate("'12345'", "'143'", "'ax'")) from customers

      val expected = "a2x5"

      val testResult = execute(query).to[String, String](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("left") {
      val query = select(Left("'abcde'", 2)) from customers

      val expected = "ab"

      val testResult = execute(query).to[String, String](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("right") {
      val query = select(Right("'abcde'", 2)) from customers

      val expected = "de"

      val testResult = execute(query).to[String, String](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("radians") {
      val query = select(Radians(45.0)) from customers

      val expected = 0.7853981634

      val testResult = execute(query).to[Double, Double](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(approximatelyEquals(expected, 10.0))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("min_scale") {
      val query = select(MinScale(8.4100)) from customers

      val expected = 2

      val testResult = execute(query).to[Int, Int](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))
      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("starts_with") {
      case class Customer(id: UUID, fname: String, lname: String, verified: Boolean, dateOfBirth: LocalDate)

      val query =
        (select(customerId ++ fName ++ lName ++ verified ++ dob) from customers).where(StartsWith(fName, """'R'"""))

      val expected =
        Seq(
          Customer(
            UUID.fromString("60b01fc9-c902-4468-8d49-3c0f989def37"),
            "Ronald",
            "Russell",
            true,
            LocalDate.parse("1983-01-05")
          )
        )

      val testResult = execute(query)
        .to[UUID, String, String, Boolean, LocalDate, Customer]((id, fname, lname, verified, dob) =>
          Customer(id, fname, lname, verified, dob)
        )

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r)(hasSameElementsDistinct(expected))

      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
    testM("upper") {
      val query = (select(Upper("first_name")) from customers).limit(1)

      val expected = "RONALD"

      val testResult = execute(query).to[String, String](identity)

      val assertion = for {
        r <- testResult.runCollect
      } yield assert(r.head)(equalTo(expected))
      
      assertion.mapErrorCause(cause => Cause.stackless(cause.untraced))
    },
  )
}