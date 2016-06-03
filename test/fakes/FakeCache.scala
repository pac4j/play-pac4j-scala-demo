package fakes

import play.api.cache.CacheApi

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

class FakeCache extends CacheApi {

  private var store = Map[String, Any]()

  override def set(key: String, value: Any, expiration: Duration): Unit =
    store = store + (key -> value)

  override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Option[T] =
    store.get(key).asInstanceOf[Option[T]]

  override def getOrElse[A](key: String, expiration: Duration)(orElse: => A)(implicit evidence$1: ClassTag[A]): A =
    store.get(key).asInstanceOf[Option[A]].getOrElse(orElse)


  override def remove(key: String): Unit =
    store = store - key
}