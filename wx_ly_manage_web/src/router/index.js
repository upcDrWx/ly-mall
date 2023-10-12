import Vue from 'vue'
import Router from 'vue-router'


Vue.use(Router)

function route (path, file, name, children) {
  return {
    exact: true,
    path,
    name,
    children,
    component: () => import('../pages' + file)
  }
}

export default new Router({
  routes: [
    route("/login",'/Login',"Login"),
    {
      path:"/",
      component: () => import('../pages/Layout'),
      // redirect:"/index/dashboard",
      redirect:"/login",
      children:[
        route("/login",'/Login',"Login"),
        route("/index/dashboard","/Dashboard","Dashboard"),
        route("/item/category",'/item/Category',"Category"),
        // route("/item/brand",'/item/Brand',"Brand"),
        //增加路由MyBrand
        route("/item/brand",'/item/MyBrand',"MyBrand"),
        // route("/item/list",'/item/Goods',"Goods"),
        route("/item/list",'/item/MyGoods',"MyGoods"),
        // route("/item/specification",'/item/Specification',"Specification"),
        route("/item/specification",'/item/specification/Specification',"Specification"),
        route("/user/statistics",'/item/Statistics',"Statistics"),
        route("/trade/promotion",'/trade/Promotion',"Promotion"),
      ]
    }
  ]
})
