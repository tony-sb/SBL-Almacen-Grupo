console.log("=== INICIANDO SISTEMA DE ÓRDENES DE COMPRA ===");

// Variable global para precios de productos
window.preciosProductos = {};
let itemCounter = 0; // Contador global de items
let botonAgregarInicializado = false;

// Función principal para agregar producto - SIMPLIFICADA
function agregarNuevoItem(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
        event.stopImmediatePropagation();
    }

    console.log(`Agregando nuevo item...`);
    console.log("itemCounter antes:", itemCounter);

    const container = document.getElementById('items-container');

    if (!container) {
        console.error("Contenedor de items no encontrado");
        alert('Error: No se puede agregar items. Recarga la página.');
        return;
    }

    // Verificar límite
    const items = container.querySelectorAll('.item-row');
    console.log("Items encontrados:", items.length);

    if (items.length >= 50) {
        console.warn("Límite de items alcanzado");
        alert('Máximo 50 productos por orden');
        return;
    }

    // Incrementar contador
    itemCounter++;
    const nuevoIndex = itemCounter;

    console.log(`Creando item con index: ${nuevoIndex}`);

    // **SIMPLIFICACIÓN: Siempre crear desde cero, no usar template**
    const nuevoItem = crearItemDesdeCero(nuevoIndex);
    nuevoItem.id = `item-${nuevoIndex}`;
    nuevoItem.classList.add('item-row', 'row', 'align-items-center', 'mb-2');

    // Insertar en el contenedor
    container.appendChild(nuevoItem);

    // Configurar el nuevo item
    configurarNuevoItem(nuevoItem, nuevoIndex);

    // Actualizar interfaz
    calcularTotal();
    actualizarContadorItems();

    console.log("Nuevo item agregado correctamente (ID:", nuevoItem.id, ")");

    // Scroll y focus al nuevo item
    setTimeout(() => {
        nuevoItem.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        const nuevoSelect = nuevoItem.querySelector('.producto-select');
        if (nuevoSelect) nuevoSelect.focus();
    }, 100);

    return nuevoItem;
}

// Función para crear item desde cero
function crearItemDesdeCero(index) {
    const div = document.createElement('div');

    // Obtener opciones de productos del primer select existente
    let opcionesProductos = '<option value="">Seleccionar producto...</option>';
    const primerSelect = document.querySelector('.producto-select');

    if (primerSelect) {
        const opciones = primerSelect.querySelectorAll('option');
        opcionesProductos = Array.from(opciones)
            .map(option => `<option value="${option.value}">${option.textContent}</option>`)
            .join('');
    }

    div.innerHTML = `
        <div class="col-md-5">
            <select class="form-select producto-select" name="productoIds">
                ${opcionesProductos}
            </select>
        </div>
        <div class="col-md-2">
            <input type="number" class="form-control cantidad-input" name="cantidades"
                   value="1" min="1" max="9999">
        </div>
        <div class="col-md-2">
            <input type="number" class="form-control precio-input" name="precios"
                   value="0.00" min="0" max="999999" step="0.01">
        </div>
        <div class="col-md-2">
            <input type="text" class="form-control subtotal-display" value="S/ 0.00" readonly>
        </div>
        <div class="col-md-1">
            <button type="button" class="btn btn-outline-danger btn-sm remove-item">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;

    return div;
}

// Función para configurar un nuevo item dinámico
function configurarNuevoItem(item, index) {
    // Limpiar valores
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const subtotalDisplay = item.querySelector('.subtotal-display');

    if (productoSelect) productoSelect.selectedIndex = 0;
    if (cantidadInput) cantidadInput.value = '1';
    if (precioInput) precioInput.value = '0.00';
    if (subtotalDisplay) subtotalDisplay.value = 'S/ 0.00';

    // Configurar eventos
    configurarEventosItem(item);

    console.log("Item configurado:", index);
}

// Configurar eventos para un item específico
function configurarEventosItem(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const removeBtn = item.querySelector('.remove-item');

    if (!productoSelect || !cantidadInput || !precioInput) {
        console.error("Elementos faltantes en item:", item);
        return;
    }
    // Evento para seleccionar producto
    productoSelect.addEventListener('change', function() {
        const productoId = this.value;
        console.log(`Producto cambiado: ${productoId}`);

        if (productoId && window.preciosProductos[productoId]) {
            precioInput.value = window.preciosProductos[productoId];
            console.log(`Precio automático: ${window.preciosProductos[productoId]}`);
            mostrarFeedbackPrecio(precioInput, 'auto');
        } else {
            if (!precioInput.value || precioInput.value === '0.00') {
                precioInput.value = '0.00';
            }
            mostrarFeedbackPrecio(precioInput, 'manual');
        }

        calcularSubtotal(item);
        calcularTotal();
        validarItemCompleto(item);
    });

    // Evento para cantidad
    cantidadInput.addEventListener('input', function() {
        if (this.value < 1) this.value = 1;
        if (this.value > 9999) this.value = 9999;
        calcularSubtotal(item);
        calcularTotal();
        validarItemCompleto(item);
    });

    cantidadInput.addEventListener('blur', function() {
        if (!this.value || this.value < 1) {
            this.value = 1;
            calcularSubtotal(item);
            calcularTotal();
        }
    });

    // Evento para precio
    precioInput.addEventListener('input', function() {
        if (this.value < 0) this.value = 0;
        if (this.value > 999999) this.value = 999999;
        calcularSubtotal(item);
        calcularTotal();
        mostrarFeedbackPrecio(this, 'manual');
        validarItemCompleto(item);
    });

    precioInput.addEventListener('blur', function() {
        if (!this.value || this.value < 0) {
            this.value = '0.00';
            calcularSubtotal(item);
            calcularTotal();
        }
    });

    // Configurar botón eliminar
    if (removeBtn) {
        // Remover eventos anteriores
        const nuevoRemoveBtn = removeBtn.cloneNode(true);
        removeBtn.parentNode.replaceChild(nuevoRemoveBtn, removeBtn);

        nuevoRemoveBtn.addEventListener('click', function() {
            console.log("Eliminar item clickeado");
            eliminarItem(item);
        });
    }

    // Calcular subtotal inicial
    calcularSubtotal(item);
    validarItemCompleto(item);
}

// Función para eliminar item
function eliminarItem(item) {
    const items = document.querySelectorAll('.item-row');
    console.log(`Total de items: ${items.length}`);

    if (items.length > 1) {
        // Animación de eliminación
        item.style.opacity = '0.5';
        item.style.backgroundColor = '#ffe6e6';

        setTimeout(() => {
            item.remove();
            calcularTotal();
            actualizarContadorItems();
            console.log("Item eliminado");
        }, 300);
    } else {
        console.log("Último item - reseteando valores...");
        resetearItem(item);
    }
}

// Función para resetear item
function resetearItem(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const subtotalDisplay = item.querySelector('.subtotal-display');

    if (productoSelect) productoSelect.selectedIndex = 0;
    if (cantidadInput) cantidadInput.value = '1';
    if (precioInput) precioInput.value = '0.00';
    if (subtotalDisplay) subtotalDisplay.value = 'S/ 0.00';

    // Resetear estilos
    item.style.borderColor = '';
    item.style.backgroundColor = '';
    item.style.opacity = '';

    calcularSubtotal(item);
}

// Calcular subtotal de un item
function calcularSubtotal(item) {
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const subtotalDisplay = item.querySelector('.subtotal-display');

    if (!cantidadInput || !precioInput || !subtotalDisplay) return;

    const cantidad = parseFloat(cantidadInput.value) || 0;
    const precio = parseFloat(precioInput.value) || 0;
    const subtotal = cantidad * precio;

    subtotalDisplay.value = 'S/ ' + subtotal.toFixed(2);
}

// Calcular total general
function calcularTotal() {
    let total = 0;
    let itemsValidos = 0;
    const items = document.querySelectorAll('.item-row');

    items.forEach(item => {
        const cantidadInput = item.querySelector('.cantidad-input');
        const precioInput = item.querySelector('.precio-input');

        if (cantidadInput && precioInput) {
            const cantidad = parseFloat(cantidadInput.value) || 0;
            const precio = parseFloat(precioInput.value) || 0;
            const subtotal = cantidad * precio;

            total += subtotal;

            const productoSelect = item.querySelector('.producto-select');
            if (cantidad > 0 && precio > 0 && productoSelect && productoSelect.value) {
                itemsValidos++;
            }
        }
    });

    // Actualizar total en formulario
    const formTotal = document.getElementById('total-orden');
    if (formTotal) {
        formTotal.textContent = 'S/ ' + total.toFixed(2);
    }

    // Actualizar total en modal si existe
    const modalTotal = document.getElementById('total-modal');
    if (modalTotal) {
        modalTotal.textContent = 'S/ ' + total.toFixed(2);
    }

    // Actualizar contador de items válidos
    const contadorItems = document.getElementById('contador-items');
    if (contadorItems) {
        contadorItems.textContent = `${itemsValidos} producto(s) válido(s)`;
        contadorItems.className = itemsValidos > 0 ? 'badge bg-success' : 'badge bg-secondary';
    }

    console.log(`Total calculado: S/ ${total.toFixed(2)} (${itemsValidos} válidos)`);
    return total;
}

// Actualizar contador de items
function actualizarContadorItems() {
    const items = document.querySelectorAll('.item-row');
    const contador = document.getElementById('contador-items-total');

    if (contador) {
        contador.textContent = `${items.length} producto(s) en la orden`;
    }
}

// Validar si un item está completo
function validarItemCompleto(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');

    const productoValido = productoSelect && productoSelect.value !== "";
    const cantidadValida = cantidadInput && parseFloat(cantidadInput.value) > 0;
    const precioValido = precioInput && parseFloat(precioInput.value) >= 0;

    const esValido = productoValido && cantidadValida && precioValido;

    // Feedback visual
    if (esValido) {
        item.style.borderColor = '#198754';
        item.style.backgroundColor = '#f8fff9';
    } else {
        item.style.borderColor = '#dee2e6';
        item.style.backgroundColor = '#f8f9fa';
    }

    return esValido;
}

// Mostrar feedback de precio
function mostrarFeedbackPrecio(input, tipo) {
    if (tipo === 'auto') {
        input.style.borderColor = '#198754';
        input.title = 'Precio cargado automáticamente';
    } else {
        input.style.borderColor = '#fd7e14';
        input.title = 'Precio ingresado manualmente';
    }
}

//Configurar botón de manera segura
function configurarBotonAgregarSeguro() {
    if (botonAgregarInicializado) {
        console.log("Botón ya inicializado, omitiendo...");
        return;
    }

    const addBtn = document.getElementById('agregarProductoBtn');
    if (!addBtn) {
        console.error("Botón 'Agregar Producto' NO encontrado");
        return;
    }

    console.log("Configurando botón de manera segura...");

    // Marcar como inicializado
    botonAgregarInicializado = true;

    // Reemplazar completamente el botón
    const nuevoBtn = document.createElement('button');
    nuevoBtn.type = 'button';
    nuevoBtn.className = addBtn.className;
    nuevoBtn.id = 'agregarProductoBtn';
    nuevoBtn.innerHTML = '<i class="bi bi-plus-circle me-1"></i>Agregar Producto';

    // Reemplazar el botón viejo
    addBtn.parentNode.replaceChild(nuevoBtn, addBtn);

    // SOLUCIÓN: Un solo event listener
    nuevoBtn.addEventListener('click', function(e) {
        console.log("🎯 Evento ÚNICO ejecutado");
        agregarNuevoItem(e);
    });

    console.log("Botón configurado con un solo listener");
}

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM cargado - Inicializando sistema de órdenes");

    // 1. Inicializar precios de productos
    inicializarPreciosProductos();

    // 2. USAR LA NUEVA FUNCIÓN SEGURA para configurar botón
    configurarBotonAgregarSeguro();

    // 3. Inicializar items existentes
    inicializarItems();

    // 4. Configurar confirmación de eliminación
    configurarConfirmacionEliminar();

    // 5. Configurar tooltips
    configurarTooltips();

    console.log("Sistema de órdenes inicializado completamente");
});

// Inicializar precios
function inicializarPreciosProductos() {
    console.log("Inicializando precios de productos...");

    if (typeof window.preciosProductos === 'undefined') {
        window.preciosProductos = {};
        console.warn("preciosProductos no definido, inicializando vacío");
    }

    console.log(`${Object.keys(window.preciosProductos).length} precios disponibles`);
}

// Inicializar items existentes
function inicializarItems() {
    console.log("Inicializando items...");
    const items = document.querySelectorAll('.item-row');
    console.log(`Encontrados ${items.length} items`);

    // Configurar contador basado en items existentes
    itemCounter = items.length;
    console.log("Contador establecido en:", itemCounter);

    items.forEach((item, index) => {
        console.log(`Inicializando item ${index + 1} (${item.id || 'sin id'})`);
        configurarEventosItem(item);
        calcularSubtotal(item);
        validarItemCompleto(item);
    });

    calcularTotal();
    actualizarContadorItems();
}

// Configurar confirmaciones de eliminación
function configurarConfirmacionEliminar() {
    console.log(" Configurando confirmaciones de eliminación...");
    document.addEventListener('click', function(e) {
        if (e.target.closest('.btn-eliminar')) {
            e.preventDefault();
            const target = e.target.closest('.btn-eliminar');
            const confirmar = confirm('¿Está seguro de eliminar esta orden?');
            if (confirmar) {
                window.location.href = target.href;
            }
        }
    });
}

// Configurar tooltips
function configurarTooltips() {
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltips = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        tooltips.forEach(el => new bootstrap.Tooltip(el));
    }
}

// Exportar funciones para debugging
window.ordenesApp = {
    agregarNuevoItem: function(e) {
        return agregarNuevoItem(e);
    },
    eliminarItem,
    calcularTotal,
    debugItems: function() {
        const items = document.querySelectorAll('.item-row');
        console.log(`=== DEBUG ITEMS (${items.length} total) ===`);
        items.forEach((item, index) => {
            const productoSelect = item.querySelector('.producto-select');
            console.log(`Item ${index + 1} (${item.id}):`, {
                producto: productoSelect?.value,
                productoNombre: productoSelect?.options[productoSelect.selectedIndex]?.text,
                valido: validarItemCompleto(item)
            });
        });
    },
    getItemCount: function() {
        return itemCounter;
    },
    // Función para reparar botón si es necesario
    repararBoton: function() {
        botonAgregarInicializado = false;
        configurarBotonAgregarSeguro();
    }
};

// Hacer funciones disponibles globalmente
window.agregarNuevoItem = agregarNuevoItem;
window.eliminarItem = eliminarItem;

console.log("Sistema de órdenes cargado y listo");

// CORRECCIÓN EXTRA: Limpiar eventos duplicados al cargar
setTimeout(function() {
    const btn = document.getElementById('agregarProductoBtn');
    if (btn) {
        // Contar event listeners (para debug)
        console.log("Verificando eventos del botón...");

        // Forzar un solo listener
        const nuevoBtn = btn.cloneNode(true);
        btn.parentNode.replaceChild(nuevoBtn, btn);

        nuevoBtn.addEventListener('click', function(e) {
            console.log("ÚNICO listener ejecutándose");
            agregarNuevoItem(e);
        });
    }
}, 1000);