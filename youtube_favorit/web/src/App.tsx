import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './app/AppShell'
import { DeveloperSettingsProvider } from './app/DeveloperSettingsContext'
import { AdminPage } from './pages/AdminPage'
import { GroupDetailPage } from './pages/GroupDetailPage'
import { GroupsPage } from './pages/GroupsPage'
import { HomePage } from './pages/HomePage'
import { MemberDetailPage } from './pages/MemberDetailPage'

function App() {
  return (
    <DeveloperSettingsProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppShell />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/groups" element={<GroupsPage />} />
            <Route path="/groups/:groupSlug" element={<GroupDetailPage />} />
            <Route path="/members/:memberSlug" element={<MemberDetailPage />} />
            <Route path="/admin" element={<AdminPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </DeveloperSettingsProvider>
  )
}

export default App
