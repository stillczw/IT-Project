// @GENERATOR:play-routes-compiler
// @SOURCE:C:/MyDoc/LESSON/ITSD-DT2021-Template/conf/routes
// @DATE:Thu Jun 17 00:00:18 BST 2021

package controllers;

import router.RoutesPrefix;

public class routes {
  
  public static final controllers.ReverseGameScreenController GameScreenController = new controllers.ReverseGameScreenController(RoutesPrefix.byNamePrefix());
  public static final controllers.ReverseAssets Assets = new controllers.ReverseAssets(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final controllers.javascript.ReverseGameScreenController GameScreenController = new controllers.javascript.ReverseGameScreenController(RoutesPrefix.byNamePrefix());
    public static final controllers.javascript.ReverseAssets Assets = new controllers.javascript.ReverseAssets(RoutesPrefix.byNamePrefix());
  }

}
