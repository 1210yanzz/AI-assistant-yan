<template>
  <el-form ref="ruleFormRef" :model="formData">
    <el-row :gutter="24">
      <template v-for="item in formDataAttrs" :key="item.props">
        <el-col v-bind="item.col">
          <el-form-item :label="item.label" :prop="item.prop">
            <component v-model="formData[item.prop]" :is="isComp(item.comp)" :placeholder="item.placeholder">
              <template v-if="item.comp === 'select'">
                <el-option label="全部" value=""/>
                <el-option v-for="opt in item.options" :key="opt.value" :label="opt.label" :value="opt.value"/>
              </template>
            </component>
          </el-form-item>
        </el-col>
      </template>
    </el-row>
    <el-row class="mt-2">
      <el-button type="primary" @click="handleRearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-row>
  </el-form>
</template>

<script setup>
import {ref, reactive, computed} from 'vue'

const props = defineProps({
  formItem: {
    type: Array,
    default: () => []
  }
})  
const emit = defineEmits(['search'])

const formDataAttrs = computed(() => {
  const { formItem} = props
  formItem.forEach(item =>{
    item.col = {xs: 24, sm: 12, md: 8, lg: 6, xl: 4}
  })
  return formItem
})

// 表单数据
const ruleFormRef = ref()
const formData = reactive({})
const isComp = (comp) =>{
  return{
    input: 'elInput',
    select: 'elSelect'
  }[comp]
}

const handleRearch = () =>{
  //console.log(formData)
  emit('search', formData)
}
const handleReset = () =>{
  // 先重置表单，再触发查询，查出原来初始的数据
  ruleFormRef.value?.resetFields()
  emit('search', formData)
}
</script>

<style lang="scss" scoped>
.mt-2{
  margin-top: -12px;
  margin-bottom: 10px;
}
</style>