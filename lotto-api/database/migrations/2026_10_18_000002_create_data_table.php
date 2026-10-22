<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('data', function (Blueprint $table): void {
            $table->id();
            $table->foreignId('gamers_id')->constrained('gamers')->cascadeOnDelete();
            $table->unsignedTinyInteger('sz1');
            $table->unsignedTinyInteger('sz2');
            $table->unsignedTinyInteger('sz3');
            $table->unsignedTinyInteger('sz4');
            $table->unsignedTinyInteger('sz5');
            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('data');
    }
};
