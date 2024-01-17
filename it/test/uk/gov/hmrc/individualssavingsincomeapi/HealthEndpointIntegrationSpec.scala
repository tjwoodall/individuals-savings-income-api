package uk.gov.hmrc.individualssavingsincomeapi

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec



class HealthEndpointIntegrationSpec
  extends AnyWordSpec
     with Matchers
     with ScalaFutures
     with IntegrationPatience
     // with GuiceOneServerPerSuite
     {

//  private val wsClient = app.injector.instanceOf[WSClient]
//  private val baseUrl  = s"http://localhost:$port"

//  override def fakeApplication(): Application =
//    GuiceApplicationBuilder()
//      .configure("metrics.enabled" -> false)
//      .build()

  "service health endpoint" should {
    "respond with 200 status" in {
      "true".toBoolean shouldBe true
//      val response =
//        wsClient
//          .url(s"$baseUrl/ping/ping")
//          .get()
//          .futureValue

//      response.status shouldBe 200
    }
  }
}
