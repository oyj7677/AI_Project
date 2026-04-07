import type { ThemeMode } from '../types/habit'

interface ThemeToggleProps {
  theme: ThemeMode
  onToggle: () => void
}

export function ThemeToggle({ theme, onToggle }: ThemeToggleProps) {
  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={onToggle}
      aria-label={`${theme === 'light' ? '다크 모드' : '라이트 모드'}로 전환`}
    >
      {theme === 'light' ? '다크 모드' : '라이트 모드'}
    </button>
  )
}
