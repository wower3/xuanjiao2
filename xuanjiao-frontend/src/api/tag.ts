import request from '@/utils/request'

export function getTagList() {
  return request.get('/tag/list')
}

export function getTagsByCategory(category: string) {
  return request.get(`/tag/list/${category}`)
}

export function createTag(data: any) {
  return request.post('/tag', null, {
    params: data
  })
}

export function deleteTag(id: number) {
  return request.delete(`/tag/${id}`)
}
