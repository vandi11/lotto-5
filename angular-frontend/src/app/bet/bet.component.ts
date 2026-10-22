import { Component, Input, OnChanges, SimpleChanges, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BaseService, LotteryTip, TipPayload } from '../services/base.service';

@Component({
  selector: 'app-bet',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="bet-panel" [attr.aria-label]="gamerName + ' tippjei'">
      <div class="bet-heading">
        <div>
          <h3>Tippek</h3>
          <p>{{ gamerName }} fogadásai és számai</p>
        </div>
        <span class="tip-count">{{ tips().length }} db</span>
      </div>

      @if (loading()) {
        <p class="bet-status" role="status">Tippek betöltése…</p>
      } @else {
        @if (error()) {
          <div class="alert alert-danger py-2" role="alert">{{ error() }}</div>
        }
        @if (message()) {
          <div class="alert alert-success py-2" role="status">{{ message() }}</div>
        }

        <div class="table-responsive bet-table-wrap">
          <table class="table table-sm align-middle bet-table">
            <thead>
              <tr>
                <th scope="col">#</th>
                <th scope="col">Szám 1</th>
                <th scope="col">Szám 2</th>
                <th scope="col">Szám 3</th>
                <th scope="col">Szám 4</th>
                <th scope="col">Szám 5</th>
                <th scope="col" class="action-heading">Művelet</th>
              </tr>
            </thead>
            <tbody>
              @for (tip of tips(); track tip.id) {
                <tr>
                  <th scope="row" class="tip-id">{{ tip.id }}</th>
                  @if (editingTipId() === tip.id) {
                    @for (number of editNumbers; track $index; let i = $index) {
                      <td><input class="form-control form-control-sm number-input" type="number" min="1" max="90" [(ngModel)]="editNumbers[i]" [ngModelOptions]="{ standalone: true }" [attr.aria-label]="'Szám ' + (i + 1)"></td>
                    }
                    <td class="row-actions">
                      <button type="button" class="btn btn-sm btn-success" (click)="saveEdit(tip)">Mentés</button>
                      <button type="button" class="btn btn-sm btn-outline-secondary" (click)="cancelEdit()">Mégse</button>
                    </td>
                  } @else {
                    @for (number of tipNumbers(tip); track $index) {
                      <td><span class="number-pill">{{ number }}</span></td>
                    }
                    <td class="row-actions">
                      <button type="button" class="btn btn-sm btn-outline-light" (click)="beginEdit(tip)">Módosít</button>
                      <button type="button" class="btn btn-sm btn-outline-danger" (click)="deleteTip(tip)">Törlés</button>
                    </td>
                  }
                </tr>
              } @empty {
                <tr><td class="empty-row" colspan="7">Ehhez a játékoshoz még nincs tipp.</td></tr>
              }
            </tbody>
          </table>
        </div>

        <form class="new-tip-form" (ngSubmit)="addTip()">
          <div class="new-tip-heading">
            <h4>Új tipp</h4>
            <span>Öt különböző szám, 1–90 között</span>
          </div>
          <div class="number-fields">
            @for (number of newNumbers; track $index; let i = $index) {
              <label class="number-field">
                <span>Szám {{ i + 1 }}</span>
                <input class="form-control" type="number" min="1" max="90" required [(ngModel)]="newNumbers[i]" [ngModelOptions]="{ standalone: true }" [attr.aria-label]="'Új tipp szám ' + (i + 1)">
              </label>
            }
            <button class="btn btn-warning add-button" type="submit" [disabled]="saving()">{{ saving() ? 'Mentés…' : 'Hozzáadás' }}</button>
          </div>
        </form>
      }
    </section>
  `,
  styleUrl: './bet.component.css',
})
export class BetComponent implements OnChanges {
  @Input({ required: true }) gamerId!: number;
  @Input() gamerName = 'Játékos';

  private readonly base = inject(BaseService);
  readonly tips = signal<LotteryTip[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly editingTipId = signal<number | null>(null);
  editNumbers = [1, 2, 3, 4, 5];
  newNumbers = [1, 2, 3, 4, 5];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['gamerId'] && Number.isFinite(this.gamerId)) this.loadTips();
  }

  loadTips(): void {
    this.loading.set(true);
    this.error.set('');
    this.base.getTips().subscribe({
      next: (allTips) => {
        this.tips.set(allTips.filter((tip) => tip.gamersId === this.gamerId).sort((a, b) => a.id - b.id));
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nem sikerült betölteni a tippeket.');
        this.loading.set(false);
      },
    });
  }

  tipNumbers(tip: LotteryTip): number[] {
    return [tip.sz1, tip.sz2, tip.sz3, tip.sz4, tip.sz5];
  }

  beginEdit(tip: LotteryTip): void {
    this.editingTipId.set(tip.id);
    this.editNumbers = this.tipNumbers(tip);
    this.error.set('');
    this.message.set('');
  }

  cancelEdit(): void {
    this.editingTipId.set(null);
  }

  saveEdit(tip: LotteryTip): void {
    const payload = this.makePayload(this.editNumbers);
    if (!payload) return;
    this.saving.set(true);
    this.error.set('');
    this.base.updateTip(tip.id, payload).subscribe({
      next: (updated) => {
        this.tips.update((items) => items.map((item) => item.id === updated.id ? updated : item));
        this.editingTipId.set(null);
        this.saving.set(false);
        this.message.set('A tipp módosítása sikerült.');
      },
      error: (error: unknown) => this.handleSaveError(error),
    });
  }

  addTip(): void {
    const payload = this.makePayload(this.newNumbers);
    if (!payload) return;
    this.saving.set(true);
    this.error.set('');
    this.message.set('');
    this.base.createTip(payload).subscribe({
      next: (created) => {
        this.tips.update((items) => [...items, created].sort((a, b) => a.id - b.id));
        this.newNumbers = [1, 2, 3, 4, 5];
        this.saving.set(false);
        this.message.set('Az új tipp mentése sikerült.');
      },
      error: (error: unknown) => this.handleSaveError(error),
    });
  }

  deleteTip(tip: LotteryTip): void {
    if (!window.confirm(`Biztosan törli a(z) ${tip.id}. számú tippet?`)) return;
    this.error.set('');
    this.message.set('');
    this.base.deleteTip(tip.id).subscribe({
      next: () => {
        this.tips.update((items) => items.filter((item) => item.id !== tip.id));
        this.message.set('A tipp törlése sikerült.');
      },
      error: () => {
        this.error.set('A tipp törlése nem sikerült. Próbálja újra.');
      },
    });
  }

  private makePayload(numbers: number[]): TipPayload | null {
    const normalized = numbers.map(Number);
    if (normalized.some((number) => !Number.isInteger(number) || number < 1 || number > 90)) {
      this.error.set('Minden szám egész szám legyen 1 és 90 között.');
      this.message.set('');
      return null;
    }
    if (new Set(normalized).size !== 5) {
      this.error.set('A tipp számai között nem lehet ismétlődés.');
      this.message.set('');
      return null;
    }
    return {
      gamersId: this.gamerId,
      sz1: normalized[0], sz2: normalized[1], sz3: normalized[2],
      sz4: normalized[3], sz5: normalized[4],
    };
  }

  private handleSaveError(error: unknown): void {
    this.saving.set(false);
    const status = typeof error === 'object' && error !== null && 'status' in error
      ? Number((error as { status: unknown }).status)
      : 0;
    this.error.set(status === 0
      ? 'Nem sikerült elérni az API-t. Ellenőrizze, hogy fut-e a Laravel szerver.'
      : 'A művelet nem sikerült. Ellenőrizze a számokat, majd próbálja újra.');
  }
}
