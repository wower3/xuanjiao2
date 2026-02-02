import request from '@/utils/request'

export function getTagList() {
  return request.post('/tag/getList', {})
}

export function getTagsByCategory(category: string) {
  return request.post('/tag/getListByCategory', { category })
}

export function createTag(data: any) {
  return request.post('/tag/create', null, {
    params: data
  })
}

export function deleteTag(id: number) {
  return request.post('/tag/delete', { id })
}
