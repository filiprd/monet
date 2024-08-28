package com.fradulovic.monet.modules

import scala.concurrent.duration.*

import com.fradulovic.monet.auth.Crypto
import com.fradulovic.monet.domain.*
import com.fradulovic.monet.http.routes.*
import com.fradulovic.monet.http.routes.secured.*

import cats.effect.Async
import cats.syntax.all.*
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s.*
import org.http4s.circe.JsonDecoder
import org.http4s.implicits.*
import org.http4s.server.Router
import org.http4s.server.middleware.*

sealed abstract class HttpApi[F[_]: Async] private (
    services: Services[F],
    security: Security[F],
    programs: Programs[F],
    crypto: Crypto
) {

  // Middlewares
  private val authMiddleware = JwtAuthMiddleware[F, AuthUser](security.jwtAuth.value, security.userAuth.findLoggedUser)

  // Open routes
  private val healthRoutes    = HealthRoutes[F](services.healthCheckService).routes
  private val accountRoutes   = AccountRoutes[F](services.accountService, crypto).routes
  private val paintingRoutes  = PaintingRoutes[F](services.paintingService).routes
  private val categoryRoutes  = CategoryRoutes[F](services.categoryService).routes
  private val loginRoutes     = LoginRoutes[F](security.userAuth).routes
  private val techniqueRoutes = TechniqueRoutes[F](services.techniqueService).routes

  // Secured routes
  private val cartRoutes            = CartRoutes[F](services.cartService).routes(authMiddleware)
  private val checkoutRoutes        = CheckoutRoutes[F](programs.checkoutProgram).routes(authMiddleware)
  private val logoutRoutes          = LogoutRoutes[F](security.userAuth).routes(authMiddleware)
  private val orderRoutes           = OrderRoutes[F](services.orderService).routes(authMiddleware)
  private val securedPaintingRoutes = SecuredPaintingRoutes[F](services.paintingService).routes(authMiddleware)

  private val allRoutes: HttpRoutes[F] =
    healthRoutes <+> accountRoutes <+> paintingRoutes <+> categoryRoutes <+> loginRoutes <+> techniqueRoutes <+>
      cartRoutes <+> checkoutRoutes <+> logoutRoutes <+> orderRoutes <+> securedPaintingRoutes

  val routes: HttpRoutes[F] = Router(
    Version.v1 -> allRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = { (http: HttpRoutes[F]) =>
    AutoSlash(http)
  } andThen { (http: HttpRoutes[F]) =>
    CORS.policy.withAllowOriginAll(http)
  } andThen { (http: HttpRoutes[F]) =>
    Timeout(60.seconds)(http)
  }

  private val loggers: HttpApp[F] => HttpApp[F] = { (http: HttpApp[F]) =>
    RequestLogger.httpApp(true, true)(http)
  } andThen { (http: HttpApp[F]) =>
    ResponseLogger.httpApp(true, true)(http)
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)
}

object HttpApi {
  def make[F[_]: Async](
      services: Services[F],
      security: Security[F],
      programs: Programs[F],
      crypto: Crypto
  ) = new HttpApi(services, security, programs, crypto) {}
}
