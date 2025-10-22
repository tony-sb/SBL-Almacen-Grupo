// Dashboard functionality
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard cargado correctamente');

    // Ejemplo de funcionalidad futura
    const productCards = document.querySelectorAll('.product-card');

    productCards.forEach(card => {
        card.addEventListener('click', function() {
            this.style.transform = 'scale(1.02)';
            setTimeout(() => {
                this.style.transform = 'scale(1)';
            }, 150);
        });
    });

    // Simular datos dinámicos (esto se reemplazará con datos reales del backend)
    updateStats();
});

function updateStats() {
    // Aquí irá la lógica para actualizar estadísticas en tiempo real
    console.log('Actualizando estadísticas del dashboard...');
}