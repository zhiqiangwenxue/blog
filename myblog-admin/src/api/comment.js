import request from '@/utils/request'

export function getCommentList(query) {
  return request({
    url: '/comment/list',
    method: 'get',
    params: query
  })
}

export function getComment(id) {
  return request({
    url: '/comment',
    method: 'get',
    params: { id }
  })
}

export function postComment(data) {
  return request({
    url: '/comment',
    method: 'post',
    data
  })
}

export function putComment(data) {
  return request({
    url: '/comment',
    method: 'put',
    data
  })
}

export function commentCount() {
  return request({
    url: '/comment/commentCount',
    method: 'get'
  })
}

export function latestComment(number) {
  return request({
    url: '/comment/latest',
    method: 'get',
    params: { number }
  })
}

export function review(data) {
  return request({
    url: '/comment/review',
    method: 'post',
    data
  })
}
