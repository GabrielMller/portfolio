import { NodeType } from "@/components/DiagramButton";

type NodeParams = {
  id?: string;
  name: string;
  type: string;
  description: string;
}
export function node(name: string, type: NodeType, description: string, id = crypto.randomUUID()) {
  return {
    id: id,
    name,
    type,
    description,
  }
}