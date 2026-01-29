package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.createDialogReducer
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.credentialsReducer
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.crudReducer
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.editDialogReducer
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.loadingReducer

val memosReducer: Reducer<MemosAction, MemosState, MemosEffect, Nothing> =
  { action, state ->
    when (action) {
      is MemosAction.Credentials -> credentialsReducer(action, state)
      is MemosAction.Loading -> loadingReducer(action, state)
      is MemosAction.Crud -> crudReducer(action, state)
      is MemosAction.CreateDialog -> createDialogReducer(action, state)
      is MemosAction.EditDialog -> editDialogReducer(action, state)
    }
  }
