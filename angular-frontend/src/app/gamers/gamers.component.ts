import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BetComponent } from '../bet/bet.component';
import { BaseService, Gamer } from '../services/base.service';

@Component({
  selector: 'app-gamers',
  standalone: true,
  imports: [RouterLink, BetComponent],
  template: `
    <main class="inner-page">
      <header class="inner-header">
        <a routerLink="/" class="brand-link" aria-label="Vissza a főoldalra">Lotto</a>
        <p>Lottó fogadások</p>
      </header>

      <section class="gamers-panel">
        <div class="panel-heading">
          <div>
            <p class="eyebrow">Nyilvántartás</p>
            <h1>Játékosok</h1>
          </div>
          @if (!loading() && !error()) {
            <span class="gamer-total">{{ gamers().length }} játékos</span>
          }
        </div>

        @if (loading()) {
          <div class="status-message" role="status">Játékosok betöltése…</div>
        } @else if (error()) {
          <div class="alert alert-danger" role="alert">
            {{ error() }}
            <button type="button" class="btn btn-sm btn-outline-danger ms-3" (click)="loadGamers()">Újrapróbálom</button>
          </div>
        } @else if (gamers().length === 0) {
          <div class="status-message">Még nincs rögzített játékos.</div>
        } @else {
          <div class="gamer-list">
            @for (gamer of gamers(); track gamer.id) {
              <article class="gamer-card">
                <div class="gamer-row">
                  <div class="gamer-identity">
                    <span class="gamer-avatar" aria-hidden="true">{{ initials(gamer.name) }}</span>
                    <div>
                      <h2>{{ gamer.name }}</h2>
                      <span class="gamer-caption">Lottójátékos</span>
                    </div>
                  </div>
                  <button
                    type="button"
                    class="btn tips-toggle"
                    [attr.aria-expanded]="isExpanded(gamer.id)"
                    [attr.aria-controls]="'tips-' + gamer.id"
                    (click)="toggleTips(gamer.id)">
                    {{ isExpanded(gamer.id) ? 'Tippek elrejtése' : 'Tippek' }}
                    <span aria-hidden="true">{{ isExpanded(gamer.id) ? '−' : '+' }}</span>
                  </button>
                </div>
                @if (isExpanded(gamer.id)) {
                  <div class="bet-wrap" [id]="'tips-' + gamer.id">
                    <app-bet [gamerId]="gamer.id" [gamerName]="gamer.name" />
                  </div>
                }
              </article>
            }
          </div>
        }
      </section>
    </main>
  `,
  styleUrl: './gamers.component.css',
})
export class GamersComponent implements OnInit {
  private readonly base = inject(BaseService);

  readonly gamers = signal<Gamer[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  private readonly expandedIds = new Set<number>();

  ngOnInit(): void {
    this.loadGamers();
  }

  loadGamers(): void {
    this.loading.set(true);
    this.error.set('');
    this.base.getGamers().subscribe({
      next: (gamers) => {
        this.gamers.set(gamers);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nem sikerült elérni az API-t. Ellenőrizze, hogy a Laravel szerver fut-e.');
        this.loading.set(false);
      },
    });
  }

  isExpanded(gamerId: number): boolean {
    return this.expandedIds.has(gamerId);
  }

  toggleTips(gamerId: number): void {
    if (this.expandedIds.has(gamerId)) this.expandedIds.delete(gamerId);
    else this.expandedIds.add(gamerId);
  }

  initials(name: string): string {
    return name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part.charAt(0)).join('').toLocaleUpperCase('hu-HU');
  }
}
