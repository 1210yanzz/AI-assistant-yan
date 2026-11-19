<template> 
 <el-dialog 
   :title="isEdit ? '编辑文章' : '新增文章'" 
   v-model="dialogVisible" 
   width="50%" 
   @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px"> 
      <el-form-item label="文章标题" prop="title"> 
        <el-input v-model="formData.title" placeholder="请输入文章标题" maxlength="200" show-word-limit clearable></el-input>
      </el-form-item>
      <el-form-item label="文章分类" prop="categoryId"> 
        <el-select v-model="formData.categoryId" placeholder="请选择文章分类">
          <el-option v-for="item in props.categoryList" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="文章摘要" prop="summary"> 
        <el-input type="textarea" v-model="formData.summary" placeholder="请输入文章摘要（可选）" maxlength="1000" show-word-limit :rows="4"></el-input>
      </el-form-item>
      <el-form-item label="标签" prop="tags"> 
        <el-select v-model="formData.tagArray" placeholder="请选择相关标签" multiple filterable allow-create style="width: 100%">
          <el-option v-for="tag in commentTags" :key="tag" :label="tag" :value="tag"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="封面图片">
        <div calss="cover-upload">
          <el-upload
           calss="avatar-upload" 
           action="#" 
           :before-upload="beforeUpload" 
           :http-request="handleUploadRequest"
           :show-file-list="false"
           accpet="image/*"
          >
           <div v-if="!imageUrl" class="cover-placeholder">
            <p>点击上传图片</p>
           </div>
           <img v-else :src="imageUrl" class="cover-image" alt="封面图片"></img>
          </el-upload>
          <div v-if="imageUrl" class="cover-remove">
            <el-button type="danger" size="small" @click="handleRemove">移除图片</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="文章内容" prop="content">
        <RichTextEditor v-model="formData.content"
        placeholder="请输入文章内容"
        :maxCharCount="5000"
        @created="handleEditorCreated"
        min-height="400px"/>
      </el-form-item>
    </el-form>
    <div v-if="btnPreview">
      <h2>内容预览</h2>
      <div v-html="formData.content"></div>
    </div>
    <template #footer>
      <el-button @click="btnPreview = !btnPreview">{{ btnPreview ? '隐藏预览' : '显示预览'}}</el-button>
      <el-button @click="handleClose">取消</el-button>
      <el-button @click="handleSumbit" type="primary" :loading="loading">{{ isEdit ? '更新文章' : '新增文章' }}</el-button>
    </template>
 </el-dialog>
</template>

<script setup> 
import { ElMessage } from 'element-plus';
import { ref, reactive, computed, nextTick, watch } from 'vue'
import { uploadFile, articleAdd, updateArticle } from '@/api/admin'
import { fileBaseUrl } from '@/config/index.js'
import RichTextEditor from '@/components/RichTextEditor.vue';

const props = defineProps({ 
  modelValue: {
   type: Boolean,
   default: false
  },
  categoryList: {
    type: Array,
    default: () => []
  },
  articleDetail: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = computed({
  get() { 
   return props.modelValue
  },
  set(value) { 
   emit('update:modelValue', value)
  }
})

const isEdit = computed(() => !!props.articleDetail?.id)

watch(() => props.articleDetail, (newValue) => { 
  if(newValue) { 
    nextTick(() => {
      Object.assign(formData, newValue)
      businessId.value = newValue.id
      imageUrl.value = fileBaseUrl + newValue.coverImage
    })
  }
})

const handleClose = () => { 
  // 清空重置表单数据
  formRef.value.resetFields()
  // 重置id
  businessId.value = null
  // 重置标签
  formData.tagArray = []
  // 重置封面和数据
  handleRemove()
  // 重置内容
  formData.content = ''
  emit('update:modelValue', false)
}

// 表单数据
const formData = reactive({
  "title": "",
  "content": "",
  "coverImage": "",
  "categoryId": '',
  "summary": "",
  "tags": "",
  "id": ""
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 200, message: '标题最多200个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { max: 5000, message: '内容最多5000个字符', trigger: 'blur' }
  ],
}

const commentTags = [
  '情绪管理', '沟通技巧', '时间管理', 
  '压力', '睡眠', '自我提升', 
  '学习技巧', '人际关系', '抑郁', 
  '放松', '心理健康', '爱', 
  '性相关', '焦虑', '自我管理', 
  '保持心态', '外部帮助', '倾诉', 
]
// 上传
const imageUrl = ref('')
// 上传前
const beforeUpload = (file) => {
  // 对上传的图片文件进行校验
  console.log(file)
  const isImage = file.type.startsWith('image/')
  const isLimit = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('上传图片封面，请选择图片文件')
    return false
  }
  if (!isLimit) {
    ElMessage.error('上传图片封面，图片文件大小不能超过5MB')
    return false
  }
  return true
}

const businessId = ref(null)
const handleUploadRequest = async ({ file }) =>{
  // UUID生成
  businessId.value = crypto.randomUUID()
  const fileRes = await uploadFile(file, {
    businessId: businessId.value
  })
  console.log(fileRes)
  // 拼接完整的图片地址
  imageUrl.value = fileBaseUrl + fileRes.filePath
  formData.coverImage = fileRes.filePath
}

const handleRemove = () => {
  imageUrl.value = ''
  formData.coverImage = ''
}

const editorInstance = ref(null)

const handleEditorCreated = (editor) => {
  editorInstance.value = editor
  // 如果是编辑模式，则将内容填充到编辑器中
  if(formData.content && editor ) {
    nextTick(() => {
      editor.setHtml(formData.content)
    })
  }
}

const btnPreview = ref(false)

const loading = ref(false)
const formRef = ref()
const handleSumbit = () => {
  formRef.value.validate((valid, fields) => { 
    if (valid) { 
      loading.value = true
    }
    console.log(formData, '表单数据')
    const data = {
      ...formData,
      tags: formData.tagArray.join(',')
    }
    delete data.tagArray

    if(!isEdit.value) { 
      // 新增
      data.id = businessId.value
      articleAdd(data).then(res => { 
        loading.value = false
        emit('success')
      })
    } else { 
      // 更新（编辑）
      updateArticle(props.articleDetail.id, data).then(res => {
        loading.value = false
        emit('success')
      })
    }
  })
}
</script>

<style lang="scss" scoped>
.cover-placeholder{
  width: 200px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8b949e;
  background: #f6f8fa;
}
.cover-image{
  width: 200px;
  height: 120px;
  display: block;
}
</style>