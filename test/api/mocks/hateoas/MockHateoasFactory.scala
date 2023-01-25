package api.mocks.hateoas

import api.hateoas.{HateoasFactory, HateoasLinksFactory, HateoasListLinksFactory}
import api.models.hateoas.{HateoasData, HateoasWrapper}
import cats.Functor
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory

import scala.language.higherKinds

trait MockHateoasFactory extends MockFactory {

  val mockHateoasFactory: HateoasFactory = mock[HateoasFactory]

  object MockHateoasFactory {

    def wrap[A, D <: HateoasData](a: A, data: D): CallHandler[HateoasWrapper[A]] = {
      (mockHateoasFactory
        .wrap(_: A, _: D)(_: HateoasLinksFactory[A, D]))
        .expects(a, data, *)
    }

    def wrapList[A[_]: Functor, I, D <: HateoasData](a: A[I], data: D): CallHandler[HateoasWrapper[A[HateoasWrapper[I]]]] = {
      (mockHateoasFactory
        .wrapList(_: A[I], _: D)(_: Functor[A], _: HateoasListLinksFactory[A, I, D]))
        .expects(a, data, *, *)
    }

  }

}
