import { useState } from 'react'
import { fetchAppConfig } from '../api/client'
import { useDeveloperSettings } from '../app/useDeveloperSettings'
import { useApiResource } from '../hooks/useApiResource'
import { formatFeatureFlagLabel, formatPlatformLabel } from '../utils/display'

export function DeveloperPanel() {
  const {
    backendUrl,
    draftBackendUrl,
    setDraftBackendUrl,
    saveDraftBackendUrl,
    connectivity,
    checkConnectivity,
    refetchVersion,
    triggerRefetch,
  } = useDeveloperSettings()
  const [open, setOpen] = useState(false)
  const appConfigResource = useApiResource(
    (signal) => fetchAppConfig(backendUrl, signal),
    [backendUrl, refetchVersion],
  )

  return (
    <aside className={`developer-panel ${open ? 'open' : 'collapsed'}`}>
      <button className="panel-toggle" onClick={() => setOpen((current) => !current)}>
        {open ? '개발자 패널 숨기기' : '개발자 패널 보기'}
      </button>

      {open ? (
        <>
          <h3>개발자 패널</h3>
          <p>브라우저에서 백엔드를 검증하기 위한 제어 패널입니다.</p>

          <label className="field">
            <span>백엔드 기본 주소</span>
            <input
              value={draftBackendUrl}
              onChange={(event) => setDraftBackendUrl(event.target.value)}
              placeholder="http://localhost:8080"
            />
          </label>

          <div className="panel-actions">
            <button onClick={saveDraftBackendUrl}>주소 저장</button>
            <button className="secondary-button" onClick={checkConnectivity}>
              {connectivity.phase === 'checking' ? '확인 중...' : '연결 확인'}
            </button>
            <button className="secondary-button" onClick={triggerRefetch}>
              전체 새로고침
            </button>
          </div>

          <dl className="panel-state">
            <div>
              <dt>현재 주소</dt>
              <dd>{backendUrl}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd>
                {connectivity.phase === 'idle'
                  ? '대기'
                  : connectivity.phase === 'checking'
                    ? '확인 중'
                    : connectivity.phase === 'online'
                      ? '온라인'
                      : '오프라인'}
              </dd>
            </div>
            <div>
              <dt>확인 시각</dt>
              <dd>
                {connectivity.checkedAt
                  ? new Date(connectivity.checkedAt).toLocaleTimeString()
                  : '-'}
              </dd>
            </div>
            <div>
              <dt>메시지</dt>
              <dd>{connectivity.message ?? '-'}</dd>
            </div>
            <div>
              <dt>앱 설정</dt>
              <dd>
                {appConfigResource.phase === 'loading'
                  ? '불러오는 중'
                  : appConfigResource.phase === 'success'
                    ? '정상'
                    : '오류'}
              </dd>
            </div>
            {appConfigResource.data?.data ? (
              <>
                <div>
                  <dt>지원 플랫폼</dt>
                  <dd>
                    {appConfigResource.data.data.runtime.data.supportedPlatforms
                      .filter((item) => item.enabled)
                      .map((item) => formatPlatformLabel(item.platform))
                      .join(', ') || '-'}
                  </dd>
                </div>
                <div>
                  <dt>기능 플래그</dt>
                  <dd>
                    실시간=
                    {formatFeatureFlagLabel(appConfigResource.data.data.featureFlags.data.enableLiveNow)}
                    , 일정=
                    {formatFeatureFlagLabel(appConfigResource.data.data.featureFlags.data.showSchedules)}
                  </dd>
                </div>
              </>
            ) : null}
          </dl>
        </>
      ) : null}
    </aside>
  )
}
