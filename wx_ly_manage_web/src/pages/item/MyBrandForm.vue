<template>
  <v-form v-model="valid" ref="myBrandForm">
    <v-text-field
      v-model="brand.name"
      label="请输入品牌名称"
      required
      :rules="[v => !!v || '品牌名称不能为空',v => v.length > 1 || '品牌名称至少2位']"
    />
    <v-text-field
      v-model="brand.letter"
      label="请输入品牌首字母"
      required
      :rules="[v => !!v || '首字母不能为空',v => /^[A-Z]{1}$/.test(v) || '品牌字母只能是A~Z的大写字母']"
    />
    <v-cascader
      url="/item/category/list"
      multiple required
      v-model="brand.categories"
      label="请选择商品分类"
    />
    <v-layout row>
      <v-flex xs4 class="subheader">
        品牌LOGO：
      </v-flex>
      <v-flex>
        <!--第一处修改：/upload修改为/upload/image-->
        <v-upload
          v-model="brand.image"
          url="/upload/image"
          :multiple="false"
          :pic-width="250"
          :pic-height="90"
        />
      </v-flex>
    </v-layout>
    <v-layout class="my-4" row>
      <v-spacer/>
      <v-btn @click="submit" color="primary">提交</v-btn>
      <v-btn @click="clear" >重置</v-btn>
    </v-layout>
  </v-form>

</template>

<script>
  export default {
    name: "my-brand-form",
    props: {
      oldBrand:{type:Object},
      isEdit: {type: Boolean, default: false},
    },
    data(){
      return{
        valid:false,
        brand:{
          id:0,
          name:"",
          letter:"",
          image:"",
          categories:[],
        },
        imageDialogVisible:false,
      }
    },
    watch:{
      oldBrand:{
        deep:true,
        handler(val){
          if(val){
            this.brand=Object.deepCopy(val);
          }else{
            this.clear();
          }
        }
      }
    },

    methods:{
      // 提交表单
      submit(){
          // 1、表单校验
        if (this.$refs.myBrandForm.validate()) {
          // 2、定义一个请求参数对象，通过解构表达式来获取brand中的属性
          this.brand.id=this.oldBrand.id;
          const {categories, letter, ...rest} = this.brand;
          // 3、数据库中只要保存分类的id即可，因此我们对categories的值进行处理,只保留id，并转为字符串
          rest.categories = categories.map(c => c.id).join(",");
          // 4、将字母都处理为大写
          rest.letter = letter.toUpperCase();
          this.verify().then(() => {
            this.$http({
              method:this.isEdit ? 'put' :'post',
              url:"/item/brand",
              data:this.$qs.stringify(rest),
            }).then(
              () =>{
                //关闭对话框
                this.$emit('reload');
                this.$message.success("保存成功！");
                this.clear();
              }
            ).catch(
              ()=>{
                this.$message.success("保存失败！");
              }
            );
          }).catch(() => {
            this.$router.push("/login");
          });
        }
      },
      clear(){
        // 重置表单
        this.$refs.myBrandForm.reset();
        // 需要手动清空商品分类
        this.categories = [];
        this.oldBrand=null;
      },
      // 图片上传出成功后操作
      handleImageSuccess(res) {
        this.brand.image = res;
      },
      removeImage(){
        this.brand.image = "";
      },
      closeWindow(){
        this.$emit("close");
      }
    },
  }
</script>

<style scoped>

</style>
