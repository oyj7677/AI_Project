# Rhythm Habit Studio

Rhythm Habit Studio is a local-first habit tracking web app built with React, Vite, and TypeScript. It helps you create habits, check in daily, track streaks, review recent history, and stay on top of progress from a single responsive dashboard.

## Features

- Create habits with a title, description, category, and frequency
- Track `daily`, `weekdays`, and `weekly` habits
- Check in for the current day or week
- View current streaks, longest streaks, and recent completion totals
- Review recent check-in history across all habits
- See dashboard stats for habits scheduled today, on-track habits, and 7/30-day activity
- Persist habits and theme preference in `localStorage`
- Use a mobile-friendly layout with a built-in dark mode toggle

## Tech Stack

- React 19
- TypeScript
- Vite
- Plain CSS with design tokens
- Vitest for streak/stat logic tests
- ESLint for linting

## Project Structure

```text
habit/
├── public/
├── src/
│   ├── components/
│   ├── hooks/
│   ├── lib/
│   ├── types/
│   ├── App.tsx
│   ├── App.css
│   ├── index.css
│   └── main.tsx
├── package.json
└── README.md
```

## How To Run

1. Open a terminal in the project folder:

   ```bash
   cd /Users/oyj/Desktop/workspace/AI_Project/habit
   ```

2. Install dependencies:

   ```bash
   npm install
   ```

3. Start the development server:

   ```bash
   npm run dev
   ```

4. Open the local URL shown in the terminal, usually:

   ```text
   http://127.0.0.1:5173/
   ```

## Helpful Scripts

- `npm run dev` starts the Vite dev server
- `npm run build` creates a production build
- `npm run test` runs the Vitest suite
- `npm run lint` runs ESLint

## How It Works

- Habits are stored in the browser using `localStorage`
- Daily and weekday habits check against the current local day
- Weekly habits count one completion per week
- Streak logic is calculated from saved completion history
- The dashboard is derived from current habit state, not hard-coded counters

## Future Improvements

- Edit existing habits instead of deleting and recreating them
- Add archived habits and recovery flows
- Support notes per check-in
- Add charts for longer-term monthly trends
- Add export/import for backups or device migration
- Add notifications or calendar integrations
