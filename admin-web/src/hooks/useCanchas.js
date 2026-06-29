import { useCollection } from './useCollection'

export function useCanchas() {
  const { items: canchas, loading, create, update, toggle } = useCollection('canchas')
  return {
    canchas,
    loading,
    createCancha: create,
    updateCancha: update,
    toggleCancha: toggle,
  }
}
