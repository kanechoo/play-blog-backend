package v1.api.log

import net.logstash.logback.marker.{LogstashMarker, Markers}
import play.api.MarkerContext
import play.api.mvc.RequestHeader

trait RequestLogMarker {

  def recodeRequestContextLog(request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker(
        "remoteAddress" -> request.remoteAddress)
    }
  }

  private implicit class ConcatLog(marker: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker.and(marker2)
  }

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)
}
