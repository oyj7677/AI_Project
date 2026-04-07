import type { ApiError, ResponseMeta } from '../types/api'

export function EnvelopeMetaBar({
  meta,
  errors,
}: {
  meta: ResponseMeta
  errors: ApiError[]
}) {
  return (
    <div className="meta-bar">
      <span>요청 ID: {meta.requestId}</span>
      <span>생성 시각: {new Date(meta.generatedAt).toLocaleString()}</span>
      <span>부분 실패: {meta.partialFailure ? '예' : '아니오'}</span>
      <span>오류 수: {errors.length}</span>
    </div>
  )
}
