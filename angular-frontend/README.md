# Lotto Angular frontend

Angular frontend for the Laravel lotto REST API. It includes the responsive home page from the assignment, a gamer list with expandable tips, and a tip table with create, edit, and delete actions.

The UI uses Bootstrap 5.3.8 CSS from the official jsDelivr CDN; custom styles provide the lottery layout and responsive behavior.

## Requirements

- Node.js 24.15+ (Angular 22 supported runtime)
- npm
- Laravel API running at `http://127.0.0.1:8000`

## Run

From this directory:

```sh
npm install
npm start
```

Open `http://localhost:4200`. Set `src/environments/environment.ts` if the Laravel API runs at a different address. Laravel's CORS configuration is already set to allow the Angular development server.

The `BaseService` centralizes API calls. Home is `/`, the gamer list is `/gamers`, and each player's `BetComponent` opens inline from the **Tippek** button. The supplied background and clover images are copied into `public/assets`.
