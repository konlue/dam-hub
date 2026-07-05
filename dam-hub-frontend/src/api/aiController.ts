// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** aiSearch POST /api/ai/search */
export async function aiSearchUsingPost(
  body: API.AiSearchRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAiSearchResponse_>('/api/ai/search', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    timeout: 120000,
    ...(options || {}),
  })
}
