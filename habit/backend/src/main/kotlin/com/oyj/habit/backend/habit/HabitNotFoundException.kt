package com.oyj.habit.backend.habit

import java.util.UUID

class HabitNotFoundException(id: UUID) :
    RuntimeException("ID가 $id 인 습관을 찾을 수 없습니다.")
