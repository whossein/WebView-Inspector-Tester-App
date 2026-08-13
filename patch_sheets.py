import sys

# Patch AiAssistantSheet
filepath = 'app/src/main/java/com/example/ui/components/AiAssistantSheet.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {"""

replacement = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {"""

if target in content:
    with open(filepath, 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched AiAssistantSheet successfully.")
else:
    print("Target not found in AiAssistantSheet.")


# Patch DomInspectorSheet
filepath_dom = 'app/src/main/java/com/example/ui/components/DomInspectorSheet.kt'
with open(filepath_dom, 'r') as f:
    content_dom = f.read()

target_dom = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.88f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {"""

replacement_dom = """    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {"""

if target_dom in content_dom:
    with open(filepath_dom, 'w') as f:
        f.write(content_dom.replace(target_dom, replacement_dom))
    print("Patched DomInspectorSheet successfully.")
else:
    print("Target not found in DomInspectorSheet.")

