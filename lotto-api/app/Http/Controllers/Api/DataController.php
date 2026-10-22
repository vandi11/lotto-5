<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Data;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;

class DataController extends Controller
{
    public function index(): JsonResponse
    {
        return response()->json(Data::query()->orderBy('id')->get()->map(fn (Data $row) => $this->toApi($row))->values());
    }

    public function store(Request $request): JsonResponse
    {
        $data = $this->validatePayload($request);
        $row = Data::create($this->toDatabase($data));

        return response()->json($this->toApi($row), 201);
    }

    public function show(int $id): JsonResponse
    {
        return response()->json($this->toApi(Data::findOrFail($id)));
    }

    public function update(Request $request, int $id): JsonResponse
    {
        $row = Data::findOrFail($id);
        $row->update($this->toDatabase($this->validatePayload($request)));

        return response()->json($this->toApi($row->refresh()));
    }

    public function destroy(int $id): JsonResponse
    {
        Data::findOrFail($id)->delete();

        return response()->json(null, 204);
    }

    /** Validate the assignment's camelCase API payload while storing snake_case columns. */
    private function validatePayload(Request $request): array
    {
        $validated = $request->validate([
            'gamersId' => ['required', 'integer', 'exists:gamers,id'],
            'sz1' => ['required', 'integer', 'between:1,90'],
            'sz2' => ['required', 'integer', 'between:1,90'],
            'sz3' => ['required', 'integer', 'between:1,90'],
            'sz4' => ['required', 'integer', 'between:1,90'],
            'sz5' => ['required', 'integer', 'between:1,90'],
        ]);

        if (count(array_unique(array_map('intval', array_slice($validated, 1)))) !== 5) {
            throw ValidationException::withMessages(['numbers' => ['A tippek között nem lehet ismétlődő szám.']]);
        }

        return $validated;
    }

    private function toDatabase(array $payload): array
    {
        return [
            'gamers_id' => $payload['gamersId'],
            'sz1' => $payload['sz1'], 'sz2' => $payload['sz2'], 'sz3' => $payload['sz3'],
            'sz4' => $payload['sz4'], 'sz5' => $payload['sz5'],
        ];
    }

    private function toApi(Data $row): array
    {
        return [
            'id' => $row->id,
            'gamersId' => $row->gamers_id,
            'sz1' => $row->sz1, 'sz2' => $row->sz2, 'sz3' => $row->sz3,
            'sz4' => $row->sz4, 'sz5' => $row->sz5,
        ];
    }
}
