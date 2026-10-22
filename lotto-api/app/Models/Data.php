<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Data extends Model
{
    protected $table = 'data';

    protected $fillable = ['gamers_id', 'sz1', 'sz2', 'sz3', 'sz4', 'sz5'];

    protected function casts(): array
    {
        return [
            'gamers_id' => 'integer',
            'sz1' => 'integer', 'sz2' => 'integer', 'sz3' => 'integer',
            'sz4' => 'integer', 'sz5' => 'integer',
        ];
    }

    public function gamer(): BelongsTo
    {
        return $this->belongsTo(Gamer::class, 'gamers_id');
    }
}
