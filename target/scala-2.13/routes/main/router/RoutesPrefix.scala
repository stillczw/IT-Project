// @GENERATOR:play-routes-compiler
// @SOURCE:C:/MyDoc/LESSON/ITSD-DT2021-Template/conf/routes
// @DATE:Thu Jun 17 00:00:18 BST 2021


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
