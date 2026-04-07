import { Link, NavLink, Outlet } from 'react-router-dom'
import { ConnectionBanner } from '../components/ConnectionBanner'
import { DeveloperPanel } from '../components/DeveloperPanel'

export function AppShell() {
  return (
    <div className="app-shell">
      <DeveloperPanel />
      <header className="site-header">
        <Link to="/" className="brand">
          <span className="brand-mark">MSN</span>
          <div>
            <strong>MyStarNow</strong>
            <p>아이돌 그룹별 공식 유튜브와 멤버 유튜브를 모아보는 허브</p>
          </div>
        </Link>
        <nav className="site-nav">
          <NavLink to="/">홈</NavLink>
          <NavLink to="/groups">그룹</NavLink>
          <NavLink to="/admin">관리자</NavLink>
        </nav>
      </header>

      <ConnectionBanner />

      <main className="page-shell">
        <Outlet />
      </main>
    </div>
  )
}
