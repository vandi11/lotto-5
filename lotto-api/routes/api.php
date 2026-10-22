<?php

use App\Http\Controllers\Api\DataController;
use App\Http\Controllers\Api\GamersController;
use Illuminate\Support\Facades\Route;

Route::get('/Data', [DataController::class, 'index']);
Route::post('/Data', [DataController::class, 'store']);
Route::get('/Data/{id}', [DataController::class, 'show'])->whereNumber('id');
Route::put('/Data/{id}', [DataController::class, 'update'])->whereNumber('id');
Route::delete('/Data/{id}', [DataController::class, 'destroy'])->whereNumber('id');

Route::get('/Gamers', [GamersController::class, 'index']);
Route::get('/Gamers/{id}', [GamersController::class, 'show'])->whereNumber('id');
