import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  template: `
    <main class="home-page">
      <h1 class="home-title">Lotto</h1>
      <section class="clover-frame" aria-label="Lottó menü">
        <a class="clover-link" routerLink="/gamers" aria-label="Játékosok és tippek megnyitása">
          <img src="/assets/lohere.png" alt="Lóhere – játékosok és tippek">
        </a>
        <a class="clover-link clover-link-reversed" routerLink="/gamers" aria-label="Tippek böngészése">
          <img src="/assets/lohere.png" alt="Lóhere – tippek böngészése">
        </a>
      </section>
    </main>
  `,
  styleUrl: './home.component.css',
})
export class HomeComponent {}
