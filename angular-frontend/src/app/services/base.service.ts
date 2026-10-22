import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LotteryTip {
  id: number;
  gamersId: number;
  sz1: number;
  sz2: number;
  sz3: number;
  sz4: number;
  sz5: number;
}

export interface Gamer {
  id: number;
  name: string;
  data: LotteryTip[];
}

export interface TipPayload {
  gamersId: number;
  sz1: number;
  sz2: number;
  sz3: number;
  sz4: number;
  sz5: number;
}

@Injectable({ providedIn: 'root' })
export class BaseService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getGamers(): Observable<Gamer[]> {
    return this.http.get<Gamer[]>(`${this.apiUrl}/Gamers`);
  }

  getTips(): Observable<LotteryTip[]> {
    return this.http.get<LotteryTip[]>(`${this.apiUrl}/Data`);
  }

  createTip(payload: TipPayload): Observable<LotteryTip> {
    return this.http.post<LotteryTip>(`${this.apiUrl}/Data`, payload);
  }

  updateTip(id: number, payload: TipPayload): Observable<LotteryTip> {
    return this.http.put<LotteryTip>(`${this.apiUrl}/Data/${id}`, payload);
  }

  deleteTip(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/Data/${id}`);
  }
}
