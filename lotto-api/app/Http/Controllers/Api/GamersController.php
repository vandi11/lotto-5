<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Gamer;
use Illuminate\Http\JsonResponse;

class GamersController extends Controller
{
    public function index(): JsonResponse
    {
        return response()->json(Gamer::query()->with('data')->orderBy('id')->get()->map(fn (Gamer $gamer) => $this->toApi($gamer))->values());
    }

    public function show(int $id): JsonResponse
    {
        return response()->json($this->toApi(Gamer::with('data')->findOrFail($id)));
    }

    private function toApi(Gamer $gamer): array
    {
        return [
            'id' => $gamer->id,
            'name' => $gamer->name,
            'data' => $gamer->data->map(fn ($row) => [
                'id' => $row->id,
                'gamersId' => $row->gamers_id,
                'sz1' => $row->sz1, 'sz2' => $row->sz2, 'sz3' => $row->sz3,
                'sz4' => $row->sz4, 'sz5' => $row->sz5,
            ])->values(),
        ];
    }
}
