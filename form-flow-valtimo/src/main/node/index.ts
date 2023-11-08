import {KotlinFileGenerator} from "@asyncapi/modelina";
import * as path from "path";
import {load} from "js-yaml";
import {readFileSync} from 'fs';

const generator = new KotlinFileGenerator();

export async function generate() {
    await generator.generateToFiles(
        load(readFileSync('../resources/config/events/form-flow-events.yml', 'utf-8')) as Record<string, unknown>,
        path.resolve(__dirname, 'model'),
        {packageName: 'com.ritense.valtimo.formflow.event'}
    );
}

if (require.main === module) {
    generate();
}