// ordenes-compra.js - Código completo y funcional
console.log("=== CARGANDO SISTEMA DE ÓRDENES DE COMPRA ===");

// Variable global para precios
let preciosProductos = {};

document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM completamente cargado - Inicializando sistema");

    // 1. Cargar precios de productos desde Thymeleaf
    cargarPreciosProductos();

    // 2. Configurar confirmación de eliminación
    configurarConfirmacionEliminar();

    // 3. Configurar según el tipo de página
    if (document.getElementById('nuevaOrdenModal')) {
        console.log("Página principal con modal detectada");
        inicializarPaginaPrincipal();
    }

    if (document.getElementById('formOrden') && !document.getElementById('nuevaOrdenModal')) {
        console.log("Página de formulario completo detectada");
        inicializarFormularioCompleto();
    }
});

// ================= FUNCIÓN PARA CARGAR PRECIOS =================
function cargarPreciosProductos() {
    console.log("Cargando precios de productos...");
    // Los precios se cargan desde Thymeleaf en el HTML
    // Esta función se llama para inicializar la variable
}

// ================= CONFIRMACIÓN DE ELIMINACIÓN =================
function configurarConfirmacionEliminar() {
    console.log("Configurando confirmaciones de eliminación...");

    document.addEventListener('click', function(e) {
        // Buscar si se hizo click en un enlace de eliminar
        let target = e.target;
        while (target && target !== document) {
            if (target.href && target.href.includes('eliminar')) {
                e.preventDefault();
                console.log("Enlace de eliminar detectado:", target.href);

                const confirmar = confirm('¿Está seguro de eliminar esta orden?');
                if (confirmar) {
                    console.log("Usuario confirmó eliminación, redirigiendo...");
                    window.location.href = target.href;
                } else {
                    console.log("Usuario canceló eliminación");
                }
                return;
            }
            target = target.parentNode;
        }
    });
}

// ================= PÁGINA PRINCIPAL (CON MODAL) =================
function inicializarPaginaPrincipal() {
    console.log("Inicializando página principal...");

    const modal = document.getElementById('nuevaOrdenModal');
    if (modal) {
        console.log("Modal encontrado, configurando eventos...");

        modal.addEventListener('show.bs.modal', function() {
            console.log("Modal abierto - inicializando contenido");
            setTimeout(() => {
                inicializarItems();
                calcularTotal();
            }, 100);
        });

        modal.addEventListener('hidden.bs.modal', function() {
            console.log("Modal cerrado");
            // No resetear para mantener los datos si el usuario cancela
        });
    } else {
        console.log("Modal no encontrado");
    }

    // Configurar botón agregar producto
    const addBtn = document.getElementById('add-item');
    if (addBtn) {
        addBtn.onclick = function() {
            agregarNuevoItem('modal');
        };
        console.log("Botón agregar producto configurado");
    }

    // Configurar validación del formulario
    const form = document.getElementById('formOrden');
    if (form) {
        form.onsubmit = validarFormulario;
        console.log("Validación de formulario configurada");
    }

    // Inicializar items existentes
    inicializarItems();
}

// ================= FORMULARIO COMPLETO =================
function inicializarFormularioCompleto() {
    console.log("Inicializando formulario completo...");

    // Configurar botón agregar producto
    const addBtn = document.getElementById('add-item');
    if (addBtn) {
        addBtn.onclick = function() {
            agregarNuevoItem('formulario');
        };
        console.log("Botón agregar producto configurado");
    }

    // Configurar validación del formulario
    const form = document.getElementById('formOrden');
    if (form) {
        form.onsubmit = validarFormulario;
        console.log("Validación de formulario configurada");
    }

    // Inicializar items existentes
    inicializarItems();
}

// ================= FUNCIONES PRINCIPALES =================
function inicializarItems() {
    console.log("Inicializando items...");
    const items = document.querySelectorAll('.item-row');
    console.log(`Encontrados ${items.length} items`);

    items.forEach((item, index) => {
        console.log(`   Inicializando item ${index + 1}`);
        configurarEventosItem(item);
        calcularSubtotal(item);
    });

    calcularTotal();
}

function configurarEventosItem(item) {
    const productoSelect = item.querySelector('.producto-select');
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const removeBtn = item.querySelector('.remove-item');

    if (!productoSelect || !cantidadInput || !precioInput || !removeBtn) {
        console.error("Elementos faltantes en item:", item);
        return;
    }

    // Remover eventos existentes
    const nuevoSelect = productoSelect.cloneNode(true);
    productoSelect.parentNode.replaceChild(nuevoSelect, productoSelect);

    const nuevaCantidad = cantidadInput.cloneNode(true);
    cantidadInput.parentNode.replaceChild(nuevaCantidad, cantidadInput);

    const nuevoPrecio = precioInput.cloneNode(true);
    precioInput.parentNode.replaceChild(nuevoPrecio, precioInput);

    // Configurar nuevos eventos
    nuevoSelect.onchange = function() {
        console.log("Producto cambiado:", this.value);
        const productoId = this.value;
        if (productoId && preciosProductos[productoId]) {
            nuevoPrecio.value = preciosProductos[productoId];
            console.log(`Precio automático: ${preciosProductos[productoId]}`);
        }
        calcularSubtotal(item);
        calcularTotal();
    };

    nuevaCantidad.oninput = function() {
        calcularSubtotal(item);
        calcularTotal();
    };

    nuevoPrecio.oninput = function() {
        calcularSubtotal(item);
        calcularTotal();
    };

    removeBtn.onclick = function() {
        console.log("🗑Intentando eliminar item...");
        eliminarItem(item);
    };

    console.log("Eventos de item configurados correctamente");
}

function agregarNuevoItem(tipo) {
    console.log(`➕ Agregando nuevo item (${tipo})...`);
    const container = document.getElementById('items-container');
    const items = container.querySelectorAll('.item-row');

    if (items.length === 0) {
        console.error("No hay items para clonar");
        alert('Error: No se puede agregar items. Recarga la página.');
        return;
    }

    const ultimoItem = items[items.length - 1];
    const nuevoItem = ultimoItem.cloneNode(true);

    // Limpiar valores del nuevo item
    const productoSelect = nuevoItem.querySelector('.producto-select');
    const cantidadInput = nuevoItem.querySelector('.cantidad-input');
    const precioInput = nuevoItem.querySelector('.precio-input');
    const subtotalDisplay = nuevoItem.querySelector('.subtotal-display');

    if (productoSelect) productoSelect.selectedIndex = 0;
    if (cantidadInput) cantidadInput.value = '1';
    if (precioInput) precioInput.value = '0.00';
    if (subtotalDisplay) subtotalDisplay.value = 'S/ 0.00';

    container.appendChild(nuevoItem);

    // Configurar eventos del nuevo item
    configurarEventosItem(nuevoItem);
    calcularTotal();

    console.log("Nuevo item agregado correctamente");
}

function eliminarItem(item) {
    const items = document.querySelectorAll('.item-row');
    console.log(`Total de items: ${items.length}`);

    if (items.length > 1) {
        item.remove();
        calcularTotal();
        console.log("Item eliminado");
    } else {
        console.log("Último item, reseteando valores...");
        // Resetear el último item en lugar de eliminarlo
        const productoSelect = item.querySelector('.producto-select');
        const cantidadInput = item.querySelector('.cantidad-input');
        const precioInput = item.querySelector('.precio-input');
        const subtotalDisplay = item.querySelector('.subtotal-display');

        if (productoSelect) productoSelect.selectedIndex = 0;
        if (cantidadInput) cantidadInput.value = '1';
        if (precioInput) precioInput.value = '0.00';
        if (subtotalDisplay) subtotalDisplay.value = 'S/ 0.00';

        calcularSubtotal(item);
        calcularTotal();
    }
}

function calcularSubtotal(item) {
    const cantidadInput = item.querySelector('.cantidad-input');
    const precioInput = item.querySelector('.precio-input');
    const subtotalDisplay = item.querySelector('.subtotal-display');

    if (!cantidadInput || !precioInput || !subtotalDisplay) {
        console.error("Elementos faltantes para calcular subtotal");
        return;
    }

    const cantidad = parseFloat(cantidadInput.value) || 0;
    const precio = parseFloat(precioInput.value) || 0;
    const subtotal = cantidad * precio;

    subtotalDisplay.value = 'S/ ' + subtotal.toFixed(2);

    console.log(`Subtotal calculado: ${cantidad} x ${precio} = ${subtotal}`);
}

function calcularTotal() {
    let total = 0;
    const items = document.querySelectorAll('.item-row');

    items.forEach(item => {
        const cantidadInput = item.querySelector('.cantidad-input');
        const precioInput = item.querySelector('.precio-input');

        if (cantidadInput && precioInput) {
            const cantidad = parseFloat(cantidadInput.value) || 0;
            const precio = parseFloat(precioInput.value) || 0;
            total += cantidad * precio;
        }
    });

    // Actualizar total en modal
    const modalTotal = document.getElementById('total-modal');
    if (modalTotal) {
        modalTotal.textContent = 'S/ ' + total.toFixed(2);
        console.log(`Total modal actualizado: S/ ${total.toFixed(2)}`);
    }

    // Actualizar total en formulario
    const formTotal = document.getElementById('total-orden');
    if (formTotal) {
        formTotal.textContent = 'S/ ' + total.toFixed(2);
        console.log(`Total formulario actualizado: S/ ${total.toFixed(2)}`);
    }
}

function validarFormulario(e) {
    console.log("Validando formulario...");

    const items = document.querySelectorAll('.item-row');
    let itemsValidos = 0;
    const productosSeleccionados = new Set();

    for (let item of items) {
        const productoSelect = item.querySelector('.producto-select');
        const cantidadInput = item.querySelector('.cantidad-input');
        const precioInput = item.querySelector('.precio-input');

        const productoId = productoSelect ? productoSelect.value : '';
        const cantidad = cantidadInput ? parseFloat(cantidadInput.value) : 0;
        const precio = precioInput ? parseFloat(precioInput.value) : 0;

        console.log(`   Item - Producto: ${productoId}, Cantidad: ${cantidad}, Precio: ${precio}`);

        if (productoId && productoId !== "" && cantidad > 0 && precio >= 0) {
            if (productosSeleccionados.has(productoId)) {
                const nombreProducto = productoSelect.options[productoSelect.selectedIndex].text;
                e.preventDefault();
                alert(`Error: El producto "${nombreProducto}" está duplicado.`);
                console.error("Producto duplicado detectado");
                return false;
            }
            productosSeleccionados.add(productoId);
            itemsValidos++;
        }
    }

    console.log(`Items válidos encontrados: ${itemsValidos}`);

    if (itemsValidos === 0) {
        e.preventDefault();
        alert('Error: Debe agregar al menos un producto válido a la orden.');
        console.error("No hay items válidos");
        return false;
    }

    console.log("Formulario validado correctamente");
    return true;
}

// Hacer funciones disponibles globalmente para debugging
window.ordenesApp = {
    inicializarItems,
    agregarNuevoItem,
    eliminarItem,
    calcularTotal,
    validarFormulario,
    preciosProductos
};

console.log("Sistema de órdenes de compra cargado completamente");