package net.thereturningvoid.bladebot.util

import scala.reflect.runtime.universe._

object EnumReflector {

  val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

  def withName[T <: Enumeration#Value: TypeTag](name: String): T = {
    typeOf[T] match {
      case valueType @ TypeRef(enumType, _, _) =>
        val methodSymbol = factoryMethodSymbol(enumType)
        val moduleSymbol = enumType.termSymbol.asModule
        reflect(moduleSymbol, methodSymbol)(name).asInstanceOf[T]
    }
  }

  def withName(clazz: Class[_], name: String): Enumeration#Value = {
    val classSymbol = mirror.classSymbol(clazz)
    val methodSymbol = factoryMethodSymbol(classSymbol.toType)
    val moduleSymbol = classSymbol.companion.asModule
    reflect(moduleSymbol, methodSymbol)(name).asInstanceOf[Enumeration#Value]
  }

  private def factoryMethodSymbol(enumType: Type): MethodSymbol = enumType.member(TermName("withName")).asMethod

  private def reflect(module: ModuleSymbol, method: MethodSymbol)(args: Any*): Any = {
    val moduleMirror = mirror.reflectModule(module)
    val instanceMirror = mirror.reflect(moduleMirror.instance)
    instanceMirror.reflectMethod(method)(args:_*)
  }
}
