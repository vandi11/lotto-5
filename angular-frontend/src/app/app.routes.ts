import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { GamersComponent } from './gamers/gamers.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, title: 'Lotto' },
  { path: 'gamers', component: GamersComponent, title: 'Játékosok | Lotto' },
  { path: '**', redirectTo: '' },
];
