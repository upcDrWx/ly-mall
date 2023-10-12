<template>
  <v-card>
    <v-card-title class="layout row">
      <v-btn color="primary" @click="addBrand">
        新增品牌
      </v-btn>
      <v-btn color="error" @click="deleteAllBrand">
        多个删除
      </v-btn>
      <v-spacer/>
      <v-text-field
        label="输入搜索条件"
        append-icon="search"
        single-line
        hide-details
        class="flex sm3"
        v-model="search"
      />
    </v-card-title>
    <v-divider/>
    <v-data-table
      :headers="headers"
      :items="brands"
      :pagination.sync="pagination"
      :total-items="totalBrands"
      :loading="loading"
      class="elevation-1"
      select-all
      v-model="selected"
    >
      <template slot="items" slot-scope="props">
        <!--添加复选框-->
        <td class="text-xs-center">
          <v-checkbox v-model="props.selected" primary hide-details>

          </v-checkbox>
        </td>
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center"><img v-if="!!props.item.image" width="102" height="36" :src="props.item.image"/></td>
        <td class="text-xs-center">{{ props.item.letter }}</td>
        <td class="justify-center layout px-1">
          <v-btn color="primary" @click="editBrand(props.item)">
            编辑
          </v-btn>

          <v-btn color="warning" @click="deleteBrand(props.item)">
            删除
          </v-btn>
        </td>
      </template>
      <!--1.躲猫猫-->
      <template slot="expand" slot-scope="props">
        <v-card flat>
          <v-card-text>Peek-a-boo!</v-card-text>
        </v-card>
      </template>

      <!--2.没查到数据-->
      <template slot="no-data">
        <v-alert :value="true" color="error" icon="warning">
          对不起，没有查询到任何数据 :(
        </v-alert>
      </template>
      <!--3.共查到多少条，当前的页数区段-->
      <template slot="pageText" slot-scope="props">
        共{{props.itemsLength}}条,当前:{{ props.pageStart }} - {{ props.pageStop }}
      </template>
    </v-data-table>
    <!--弹出的对话框-->
    <v-dialog max-width="500" v-model="show" persistent scrollable v-if="show">
      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{isEdit ? '修改品牌' : '新增品牌'}}</v-toolbar-title>
          <v-spacer/>
          <!--关闭窗口的按钮-->
          <v-btn icon @click="show = false">
            <v-icon>close</v-icon>
          </v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-5 py-2" style="height:600px;">
          <my-brand-form :oldBrand="brand" :isEdit="isEdit" @close="show = false" :reload="loadBrands"
                         v-bind:oldBrand="oldBrand"/>
          <!--<my-brand-form @reload="reload" v-bind:isEdit="isEdit" v-bind:oldBrand="oldBrand"></my-brand-form>-->
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
  // 导入自定义的表单组件
  import MyBrandForm from './MyBrandForm'
  export default {
    name: "my-brand",
    //子组件
    components:{
      MyBrandForm
    },
    data() {
      return {
        headers: [
          {text: "品牌id", value: "id", align: 'center', sortable: true},
          {text: "品牌名称", value: "name", align: 'center', sortable: false},
          {text: "品牌LOGO", value: "image", align: 'center', sortable: false},
          {text: "品牌首字母", value: "letter", align: 'center', sortable: true},
          {text: "操作", align: 'center', sortable: false},
        ],
        brands: [],
        pagination: {},
        totalBrands: 0,
        loading: false,
        search:"",//搜索条件
        show:false,
        oldBrands:{},//回显要修改的数据

        //多了以下两个model
        brand: {}, // 品牌信息,单个品牌时使用
        isEdit: false,// 判断是编辑还是新增

        selected:[] //选择的条目
      }
    },

    //vue的周期函数，一般可以在created函数中调用ajax获取页面初始化所需的数据
    created(){
      this.verify().then(() => {

      }).catch(() => {
        this.$router.push("/login");
      });
      this.loadBrands();
      //去后台查询
    },
    //监控
    watch:{
      search(){
        //当查询时，将分页强制成‘1’
        this.pagination.page = 1;
        this.loadBrands();
      },
      //深度监控
      pagination:{
        deep:true,
        handler(){
          this.loadBrands();
        }
      },
      show(val, oldVal) {
        // 表单关闭后重新加载数据
        if (oldVal) {
          this.loadBrands();
        }
      }
    },
    created(){
      this.verify().then(() => {
        this.loadBrands();
      }).catch(() => {
        this.$router.push("/login");
      });
    },
    methods:{
      loadBrands(){
        //开启进度条
        this.loading = true;
        // this.$http.get('请求地址')来发送请求
        //使用this.$http.get('data/cartData.json')获取json中的数据，后面可加参数。
        //then()方法异步执行，就是当then()前面的方法执行完之后在执行then()里面的方法，这样就不会发生获取不到数据的问题。
        this.$http.get("/item/brand/page",{
          params:{
            page:this.pagination.page,//当前页码
            rows:this.pagination.rowsPerPage,//每页大小
            sortBy:this.pagination.sortBy,//排序字段
            desc:this.pagination.descending,//是否降序
            search:this.search,//搜索条件
          }
        }).then(resp=>{
          //会把响应得到的数据赋值给对应的model
          this.brands = resp.data.items;
          this.totalBrands=resp.data.total;
          //关闭进度条
          this.loading = false;
        })
      },
      //新增品牌时调用
      addBrand() {
        this.verify().then(() => {
          //新增窗口打开前，把数据置空
          this.oldBrand=null;
          this.brand = {};
          this.isEdit = false;
          this.show = true;

        }).catch(() => {
          this.$router.push("/login");
        });
      },
      //编辑按钮时调用
      editBrand(oldBrand){
        //根据品牌信息查询商品分类
        this.verify().then(() => {
          const bid=oldBrand.id;
          // console.log("编辑获取id"+bid);
          //根据品牌信息查询商品分类
          this.$http.get("/item/category/bid/"+bid).then(
            ({data}) => {
              this.isEdit=true;
              //显示弹窗
              this.show=true;
              //获取要编辑的brand
              this.oldBrand=oldBrand;
              this.oldBrand.categories = data;
            }
          ).catch();
        }).catch(() => {
          this.$router.push("/login");
        });
      },

      //删除按钮时调用
      deleteBrand(oldBrand){
        if (this.selected.length === 1 && this.selected[0].id === oldBrand.id) {
            if (oldBrand.id!=null) {
              this.$message.confirm('此操作将永久删除该品牌, 是否继续?').then(
                () => {
                  //发起删除请求，删除单条数据
                  this.$http.delete("/item/brand/bid/" + oldBrand.id).then(() => {
                    this.$message.success("删除成功！");
                    this.loadBrands();
                  }).catch(() => {
                    this.$message.error("删除失败！");
                  })
                }
              ).catch(() => {
                this.$message.info("删除已取消！");
              });
            }
        }
      },
      deleteAllBrand(){
        //拼接id数组
        /**
         * 加了{}就必须有return
         * @type {any[]}
         */
        const ids = this.selected.map( s => s.id);
        if (ids.length > 0) {
            this.$message.confirm('此操作将永久删除所选品牌，是否继续?').then(
              () => {
                this.$http.delete("/item/brand/bid/" + ids.join("-")).then(() => {
                  this.loadBrands();
                }).catch();
              }
            ).catch(() => {
              this.$message.info("删除已取消！");
            });
        }
      },
    },

  }
</script>

<style scoped>

</style>
