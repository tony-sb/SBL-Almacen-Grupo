document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard cargado correctamente');

    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    const tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    const productCards = document.querySelectorAll('.card');

    productCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.transition = 'all 0.3s ease';
            this.style.boxShadow = '0 4px 15px rgba(0,0,0,0.1)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 2px 5px rgba(0,0,0,0.1)';
        });
    });

    animateStatsCards();

    setInterval(updateTimestamp, 60000);

    initDashboardFeatures();
});

function animateStatsCards() {
    const statCards = document.querySelectorAll('.card.text-white');

    statCards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(20px)';
            card.style.transition = 'all 0.5s ease';

            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0)';
            }, 100);
        }, index * 200);
    });
}

function updateTimestamp() {
    const now = new Date();
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    };

    console.log('Dashboard actualizado:', now.toLocaleDateString('es-ES', options));
}

function initDashboardFeatures() {
    // Funcionalidad para el botón de descargar PDF
    const downloadBtn = document.querySelector('a[th\\:href="@{/descargar-inventario}"]');
    if (downloadBtn) {
        downloadBtn.addEventListener('click', function(e) {
            console.log('📥 Iniciando descarga de reporte PDF...');
            // Aquí puedes agregar un loader o confirmación
            this.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Generando PDF...';

            setTimeout(() => {
                this.innerHTML = '<i class="bi bi-download me-1"></i>Descargar Reporte PDF';
            }, 2000);
        });
    }

    const alertSection = document.querySelector('.alert.alert-warning');
    if (alertSection) {
        alertSection.addEventListener('click', function() {
            this.style.opacity = '0.8';
            setTimeout(() => {
                this.style.opacity = '1';
            }, 300);
        });
    }

    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.backgroundColor = '#f8f9fa';
            this.style.cursor = 'pointer';
        });

        row.addEventListener('mouseleave', function() {
            this.style.backgroundColor = '';
        });

        row.addEventListener('click', function() {
            const productName = this.cells[0].textContent;
            console.log('Ver detalles de:', productName);
        });
    });
}

function updateStats() {
    console.log('Actualizando estadísticas del dashboard...');

    // Simular actualización de datos
    const stats = {
        productos: Math.floor(Math.random() * 10) + 20,
        ordenes: Math.floor(Math.random() * 5) + 5,
        stockBajo: Math.floor(Math.random() * 3) + 1,
        movimientos: Math.floor(Math.random() * 8) + 4
    };

    fetch('/api/dashboard/stats')
        .then(response => response.json())
        .then(data => {
            updateStatsDisplay(data);
        })
        .catch(error => {
            console.warn('No se pudieron cargar estadísticas en tiempo real:', error);
        });
}

function updateStatsDisplay(stats) {
    console.log('Actualizando display con:', stats);
}

window.addEventListener('error', function(e) {
    console.error('Error en el dashboard:', e.error);
});

window.dashboard = {
    updateStats,
    animateStatsCards
};